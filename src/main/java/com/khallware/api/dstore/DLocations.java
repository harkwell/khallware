// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.dstore;

import com.khallware.api.Datastore.Wrapper;
import com.khallware.api.DatastoreException;
import com.khallware.api.dstore.Operator.Operation;
import com.khallware.api.domain.LocationTags;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.Tag;
import com.khallware.api.domain.Location;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.dao.Dao;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DLocations extends APICrudChain<Location>
{
	private static final Logger logger = LoggerFactory.getLogger(
		DLocations.class);

	@Override
	public Class onGetClass()
	{
		return(Location.class);
	}

	@Override
	public Location onCreate(final Location location)
			throws DatastoreException
	{
		Operator.<Location>perform((dao) -> {
			location.preSave();
			dao.createOrUpdate(location);
		}, Location.class);
		return(location);
	}

	@Override
	public Location onRead(final long id) throws DatastoreException
	{
		final Wrapper<Location> retval = new Wrapper<>();
		Operator.<Location>perform((dao) -> {
			retval.item = dao.queryForId((int)id);
		}, Location.class);
		return(retval.item);
	}

	@Override
	public List<Location> onRead(final Pagination pg,
			final Credentials creds, final Location pattern)
			throws DatastoreException
	{
		final Wrapper<List<Location>> retval = new Wrapper<>();
		Operator.<Location>perform((dao) -> {
			QueryBuilder<Location, Integer> qb = dao.queryBuilder()
				.setCountOf(pg.returnCount())
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			retval.item = new ArrayList<Location>();
			filterOnMode(qb.where(), creds);
			retval.count = pg.returnCount() ? qb.countOf() : -1;
			retval.item.addAll((pattern == null)
				? qb.query()
				: dao.queryForMatchingArgs(pattern));
		}, Location.class);
		pg.setCount(retval.count);
		return(retval.item);
	}

	@Override
	public List<Location> onRead(final Tag tag, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		final List<Location> retval = new ArrayList<>();
		Operator.<LocationTags>perform((dao) -> {
			QueryBuilder<LocationTags, Integer> qb =
				dao.queryBuilder()
					.setCountOf(pg.returnCount())
					.offset(pg.calcCursorIndex())
					.limit(pg.getPageSize());
			qb.where().eq(LocationTags.COL_TAG, tag);

			if (!pg.returnCount()) {
				for (LocationTags lt : qb.query()) {
					retval.add(lt.getLocation());
				}
			}
			pg.setCount(pg.returnCount() ? qb.countOf() : -1);
		}, LocationTags.class);
		return(retval);
	}

	@Override
	public Location onUpdate(final Location location)
			throws DatastoreException
	{
		Operator.<Location>perform((dao) -> dao.update(location),
			Location.class);
		return(location);
	}

	@Override
	public Location onDelete(final Location location)
			throws DatastoreException
	{
		Operator.<Location>perform((dao) -> dao.delete(location),
			Location.class);
		return(location);
	}

	@Override
	public Location onAdd2Tag(final Location location, final Tag tag)
			throws DatastoreException
	{
		Operator.<LocationTags>perform((dao) -> {
			dao.createOrUpdate(new LocationTags(location, tag));
		}, LocationTags.class);
		return(location);
	}

	@Override
	public Location onDeleteFromTags(final Location location, final Tag tag)
			throws DatastoreException
	{
		Operator.<LocationTags>perform((dao) -> {
			dao.delete(new LocationTags(location, tag));
		}, LocationTags.class);
		return(location);
	}

	@Override
	public List<Tag> onGetTags(final Location loc, final Pagination pg)
			throws DatastoreException
	{
		final List<Tag> retval = new ArrayList<>();
		Operator.<LocationTags>perform((dao) -> {
			QueryBuilder<LocationTags, Integer> qb =
				dao.queryBuilder()
					.offset(pg.calcCursorIndex())
					.limit(pg.getPageSize());
			qb.where().eq(LocationTags.COL_LOCATION, loc);

			for (LocationTags lt : qb.query()) {
				retval.add(lt.getTag());
			}
		}, LocationTags.class);
		return(retval);
	}
}
