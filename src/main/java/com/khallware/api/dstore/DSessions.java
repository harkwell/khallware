// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.dstore;

import com.khallware.api.Datastore.Wrapper;
import com.khallware.api.DatastoreException;
import com.khallware.api.dstore.Operator.Operation;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.Session;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.dao.Dao;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DSessions extends APICrudChain<Session>
{
	private static final Logger logger = LoggerFactory.getLogger(
		DSessions.class);

	@Override
	public Class onGetClass()
	{
		return(Session.class);
	}

	@Override
	public Session onCreate(final Session session)
			throws DatastoreException
	{
		Operator.<Session>perform((dao) -> {
			session.preSave();
			dao.createOrUpdate(session);
		}, Session.class);
		return(session);
	}

	@Override
	public Session onRead(final long id) throws DatastoreException
	{
		final Wrapper<Session> retval = new Wrapper<>();
		Operator.<Session>perform((dao) -> {
			retval.item = dao.queryForId((int)id);
		}, Session.class);
		return(retval.item);
	}

	@Override
	public List<Session> onRead(final Pagination pg,
			final Credentials ignored, final Session pattern)
			throws DatastoreException
	{
		final Wrapper<List<Session>> retval = new Wrapper<>();
		Operator.<Session>perform((dao) -> {
			QueryBuilder<Session, Integer> qb = dao.queryBuilder()
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			retval.item = new ArrayList<Session>();
			retval.item.addAll((pattern == null)
				? qb.query()
				: dao.queryForMatchingArgs(pattern));
		}, Session.class);
		return(retval.item);
	}

	@Override
	public Session onUpdate(final Session session)
			throws DatastoreException
	{
		Operator.<Session>perform((dao) -> dao.update(session),
			Session.class);
		return(session);
	}

	@Override
	public Session onDelete(final Session session)
			throws DatastoreException
	{
		Operator.<Session>perform((dao) -> dao.delete(session),
			Session.class);
		return(session);
	}
}
