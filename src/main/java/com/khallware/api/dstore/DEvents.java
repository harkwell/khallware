// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.dstore;

import com.khallware.api.Datastore.Wrapper;
import com.khallware.api.DatastoreException;
import com.khallware.api.dstore.Operator.Operation;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.EventTags;
import com.khallware.api.domain.Tag;
import com.khallware.api.domain.Event;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.dao.Dao;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DEvents extends APICrudChain<Event>
{
	private static final Logger logger = LoggerFactory.getLogger(
		DEvents.class);

	@Override
	public Class onGetClass()
	{
		return(Event.class);
	}

	@Override
	public Event onCreate(final Event event)
			throws DatastoreException
	{
		Operator.<Event>perform((dao) -> {
			event.preSave();
			dao.createOrUpdate(event);
		}, Event.class);
		return(event);
	}

	@Override
	public Event onRead(final long id) throws DatastoreException
	{
		final Wrapper<Event> retval = new Wrapper<>();
		Operator.<Event>perform((dao) -> {
			retval.item = dao.queryForId((int)id);
		}, Event.class);
		return(retval.item);
	}

	@Override
	public List<Event> onRead(final Pagination pg,
			final Credentials creds, final Event pattern)
			throws DatastoreException
	{
		final Wrapper<List<Event>> retval = new Wrapper<>();
		Operator.<Event>perform((dao) -> {
			QueryBuilder<Event, Integer> qb = dao.queryBuilder()
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			retval.item = new ArrayList<Event>();
			filterOnMode(qb.where(), creds);
			retval.item.addAll((pattern == null)
				? qb.query()
				: dao.queryForMatchingArgs(pattern));
		}, Event.class);
		return(retval.item);
	}

	@Override
	public List<Event> onRead(final Tag tag, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		final List<Event> retval = new ArrayList<>();
		Operator.<EventTags>perform((dao) -> {
			QueryBuilder<EventTags, Integer> qb = dao.queryBuilder()
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			qb.where().eq(EventTags.COL_TAG, tag);

			for (EventTags et : qb.query()) {
				retval.add(et.getEvent());
			}
		}, EventTags.class);
		return(retval);
	}

	@Override
	public Event onUpdate(final Event event)
			throws DatastoreException
	{
		Operator.<Event>perform((dao) -> dao.update(event),Event.class);
		return(event);
	}

	@Override
	public Event onDelete(final Event event)
			throws DatastoreException
	{
		Operator.<Event>perform((dao) -> dao.delete(event),Event.class);
		return(event);
	}

	@Override
	public Event onAdd2Tag(final Event event, final Tag tag)
			throws DatastoreException
	{
		Operator.<EventTags>perform((dao) -> {
			dao.createOrUpdate(new EventTags(event, tag));
		}, EventTags.class);
		return(event);
	}

	@Override
	public Event onDeleteFromTags(final Event event, final Tag tag)
			throws DatastoreException
	{
		Operator.<EventTags>perform((dao) -> {
			dao.delete(new EventTags(event, tag));
		}, EventTags.class);
		return(event);
	}

	@Override
	public List<Tag> onGetTags(final Event event, final Pagination pg)
			throws DatastoreException
	{
		final List<Tag> retval = new ArrayList<>();
		Operator.<EventTags>perform((dao) -> {
			QueryBuilder<EventTags, Integer> qb = dao.queryBuilder()
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			qb.where().eq(EventTags.COL_EVENT, event);

			for (EventTags et : qb.query()) {
				retval.add(et.getTag());
			}
		}, EventTags.class);
		return(retval);
	}
}
