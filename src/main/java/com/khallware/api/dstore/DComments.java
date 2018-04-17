// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.dstore;

import com.khallware.api.Datastore.Wrapper;
import com.khallware.api.DatastoreException;
import com.khallware.api.dstore.Operator.Operation;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.Comment;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.dao.Dao;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DComments extends APICrudChain<Comment>
{
	private static final Logger logger = LoggerFactory.getLogger(
		DComments.class);

	@Override
	public Class onGetClass()
	{
		return(Comment.class);
	}

	@Override
	public Comment onCreate(final Comment comment)
			throws DatastoreException
	{
		Operator.<Comment>perform((dao) -> {
			comment.preSave();
			dao.createOrUpdate(comment);
		}, Comment.class);
		return(comment);
	}

	@Override
	public Comment onRead(final long id) throws DatastoreException
	{
		final Wrapper<Comment> retval = new Wrapper<>();
		Operator.<Comment>perform((dao) -> {
			retval.item = dao.queryForId((int)id);
		}, Comment.class);
		return(retval.item);
	}

	@Override
	public List<Comment> onRead(final Pagination pg,
			final Credentials creds, final Comment pattern)
			throws DatastoreException
	{
		final Wrapper<List<Comment>> retval = new Wrapper<>();
		Operator.<Comment>perform((dao) -> {
			QueryBuilder<Comment, Integer> qb = dao.queryBuilder()
				.setCountOf(pg.returnCount())
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			retval.item = new ArrayList<Comment>();
			filterOnMode(qb.where(), creds);
			retval.count = pg.returnCount() ? qb.countOf() : -1;
			retval.item.addAll((pattern == null)
				? qb.query()
				: dao.queryForMatchingArgs(pattern));
		}, Comment.class);
		pg.setCount(retval.count);
		return(retval.item);
	}

	@Override
	public Comment onUpdate(final Comment comment)
			throws DatastoreException
	{
		Operator.<Comment>perform((dao) -> dao.update(comment),
			Comment.class);
		return(comment);
	}

	@Override
	public Comment onDelete(final Comment comment)
			throws DatastoreException
	{
		Operator.<Comment>perform((dao) -> dao.delete(comment),
			Comment.class);
		return(comment);
	}
}
