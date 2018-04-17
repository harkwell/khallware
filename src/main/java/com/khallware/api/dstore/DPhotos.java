// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.dstore;

import com.khallware.api.Datastore.Wrapper;
import com.khallware.api.DatastoreException;
import com.khallware.api.dstore.Operator.Operation;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.PhotoTags;
import com.khallware.api.domain.Tag;
import com.khallware.api.domain.Photo;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.dao.Dao;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DPhotos extends APICrudChain<Photo>
{
	private static final Logger logger = LoggerFactory.getLogger(
		DPhotos.class);

	@Override
	public Class onGetClass()
	{
		return(Photo.class);
	}

	@Override
	public Photo onCreate(final Photo photo)
			throws DatastoreException
	{
		Operator.<Photo>perform((dao) -> {
			photo.preSave();
			dao.createOrUpdate(photo);
		}, Photo.class);
		return(photo);
	}

	@Override
	public Photo onRead(final long id) throws DatastoreException
	{
		final Wrapper<Photo> retval = new Wrapper<>();
		Operator.<Photo>perform((dao) -> {
			retval.item = dao.queryForId((int)id);
		}, Photo.class);
		return(retval.item);
	}

	@Override
	public List<Photo> onRead(final Pagination pg,
			final Credentials creds, final Photo pattern)
			throws DatastoreException
	{
		final Wrapper<List<Photo>> retval = new Wrapper<>();
		Operator.<Photo>perform((dao) -> {
			QueryBuilder<Photo, Integer> qb = dao.queryBuilder()
				.setCountOf(pg.returnCount())
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			retval.item = new ArrayList<Photo>();
			filterOnMode(qb.where(), creds);
			retval.count = pg.returnCount() ? qb.countOf() : -1;
			retval.item.addAll((pattern == null)
				? qb.query()
				: dao.queryForMatchingArgs(pattern));
		}, Photo.class);
		pg.setCount(retval.count);
		return(retval.item);
	}

	@Override
	public List<Photo> onRead(final Tag tag, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		final List<Photo> retval = new ArrayList<>();
		Operator.<PhotoTags>perform((dao) -> {
			QueryBuilder<PhotoTags, Integer> qb = dao.queryBuilder()
				.setCountOf(pg.returnCount())
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			qb.where().eq(PhotoTags.COL_TAG, tag);

			if (!pg.returnCount()) {
				for (PhotoTags pt : qb.query()) {
					retval.add(pt.getPhoto());
				}
			}
			pg.setCount(pg.returnCount() ? qb.countOf() : -1);
		}, PhotoTags.class);
		return(retval);
	}

	@Override
	public Photo onUpdate(final Photo photo)
			throws DatastoreException
	{
		Operator.<Photo>perform((dao) -> dao.update(photo),Photo.class);
		return(photo);
	}

	@Override
	public Photo onDelete(final Photo photo)
			throws DatastoreException
	{
		Operator.<Photo>perform((dao) -> dao.delete(photo),Photo.class);
		return(photo);
	}

	@Override
	public Photo onAdd2Tag(final Photo photo, final Tag tag)
			throws DatastoreException
	{
		Operator.<PhotoTags>perform((dao) -> {
			dao.createOrUpdate(new PhotoTags(photo, tag));
		}, PhotoTags.class);
		return(photo);
	}

	@Override
	public Photo onDeleteFromTags(final Photo photo, final Tag tag)
			throws DatastoreException
	{
		Operator.<PhotoTags>perform((dao) -> {
			dao.delete(new PhotoTags(photo, tag));
		}, PhotoTags.class);
		return(photo);
	}

	@Override
	public List<Tag> onGetTags(final Photo photo, final Pagination pg)
			throws DatastoreException
	{
		final List<Tag> retval = new ArrayList<>();
		Operator.<PhotoTags>perform((dao) -> {
			QueryBuilder<PhotoTags, Integer> qb = dao.queryBuilder()
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			qb.where().eq(PhotoTags.COL_PHOTO, photo);

			for (PhotoTags pt : qb.query()) {
				retval.add(pt.getTag());
			}
		}, PhotoTags.class);
		return(retval);
	}
}
