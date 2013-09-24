// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.dstore;

import com.khallware.api.Datastore.Wrapper;
import com.khallware.api.DatastoreException;
import com.khallware.api.dstore.Operator.Operation;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.Tag;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.dao.Dao;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DTags extends APICrudChain<Tag>
{
	private static final Logger logger = LoggerFactory.getLogger(
		DTags.class);

	@Override
	public Class onGetClass()
	{
		return(Tag.class);
	}

	@Override
	public Tag onCreate(final Tag tag)
			throws DatastoreException
	{
		Operator.<Tag>perform((dao) -> {
			tag.preSave();
			dao.createOrUpdate(tag);
		}, Tag.class);
		return(tag);
	}

	@Override
	public Tag onRead(final long id) throws DatastoreException
	{
		if (id == Tag.ROOT) {
			return(new Tag());
		}
		final Wrapper<Tag> retval = new Wrapper<>();
		Operator.<Tag>perform((dao) -> {
			retval.item = dao.queryForId((int)id);
		}, Tag.class);
		return(retval.item);
	}

	@Override
	public List<Tag> onRead(final Pagination pg,
			final Credentials creds, final Tag pattern)
			throws DatastoreException
	{
		final Wrapper<List<Tag>> retval = new Wrapper<>();
		Operator.<Tag>perform((dao) -> {
			QueryBuilder<Tag, Integer> qb = dao.queryBuilder()
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			filterOnMode(qb.where(), creds);
			retval.item = new ArrayList<Tag>();

			if (pg.isSorted()) {
				qb.orderBy(pg.getSortColumn(),
					pg.isSortAscending());
			}
			if (pg.returnCount()) {
				pg.setCount(qb.countOf());
			}
			retval.item.addAll(qb.query());
		}, Tag.class);
		return(retval.item);
	}

	@Override
	public Tag onUpdate(final Tag tag)
			throws DatastoreException
	{
		Operator.<Tag>perform((dao) -> {
			dao.update(tag);
		}, Tag.class);
		return(tag);
	}

	@Override
	public Tag onDelete(final Tag tag)
			throws DatastoreException
	{
		Operator.<Tag>perform((dao) -> dao.delete(tag), Tag.class);
		return(tag);
	}
}
