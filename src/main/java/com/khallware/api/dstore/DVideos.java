// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.dstore;

import com.khallware.api.Datastore.Wrapper;
import com.khallware.api.DatastoreException;
import com.khallware.api.dstore.Operator.Operation;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.VideoTags;
import com.khallware.api.domain.Tag;
import com.khallware.api.domain.Video;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.dao.Dao;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DVideos extends APICrudChain<Video>
{
	private static final Logger logger = LoggerFactory.getLogger(
		DVideos.class);

	@Override
	public Class onGetClass()
	{
		return(Video.class);
	}

	@Override
	public Video onCreate(final Video video)
			throws DatastoreException
	{
		Operator.<Video>perform((dao) -> {
			video.preSave();
			dao.createOrUpdate(video);
		}, Video.class);
		return(video);
	}

	@Override
	public Video onRead(final long id) throws DatastoreException
	{
		final Wrapper<Video> retval = new Wrapper<>();
		Operator.<Video>perform((dao) -> {
			retval.item = dao.queryForId((int)id);
		}, Video.class);
		return(retval.item);
	}

	@Override
	public List<Video> onRead(final Pagination pg,
			final Credentials creds, final Video pattern)
			throws DatastoreException
	{
		final Wrapper<List<Video>> retval = new Wrapper<>();
		Operator.<Video>perform((dao) -> {
			QueryBuilder<Video, Integer> qb = dao.queryBuilder()
				.setCountOf(pg.returnCount())
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			retval.item = new ArrayList<Video>();
			filterOnMode(qb.where(), creds);
			retval.count = pg.returnCount() ? qb.countOf() : -1;
			retval.item.addAll((pattern == null)
				? qb.query()
				: dao.queryForMatchingArgs(pattern));
		}, Video.class);
		pg.setCount(retval.count);
		return(retval.item);
	}

	@Override
	public List<Video> onRead(final Tag tag, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		final List<Video> retval = new ArrayList<>();
		Operator.<VideoTags>perform((dao) -> {
			QueryBuilder<VideoTags, Integer> qb = dao.queryBuilder()
				.setCountOf(pg.returnCount())
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			qb.where().eq(VideoTags.COL_TAG, tag);

			if (!pg.returnCount()) {
				for (VideoTags vt : qb.query()) {
					retval.add(vt.getVideo());
				}
			}
			pg.setCount(pg.returnCount() ? qb.countOf() : -1);
		}, VideoTags.class);
		return(retval);
	}

	@Override
	public Video onUpdate(final Video video)
			throws DatastoreException
	{
		Operator.<Video>perform((dao) -> dao.update(video),Video.class);
		return(video);
	}

	@Override
	public Video onDelete(final Video video)
			throws DatastoreException
	{
		Operator.<Video>perform((dao) -> dao.delete(video),Video.class);
		return(video);
	}

	@Override
	public Video onAdd2Tag(final Video video, final Tag tag)
			throws DatastoreException
	{
		Operator.<VideoTags>perform((dao) -> {
			dao.createOrUpdate(new VideoTags(video, tag));
		}, VideoTags.class);
		return(video);
	}

	@Override
	public Video onDeleteFromTags(final Video video, final Tag tag)
			throws DatastoreException
	{
		Operator.<VideoTags>perform((dao) -> {
			dao.delete(new VideoTags(video, tag));
		}, VideoTags.class);
		return(video);
	}

	@Override
	public List<Tag> onGetTags(final Video video, final Pagination pg)
			throws DatastoreException
	{
		final List<Tag> retval = new ArrayList<>();
		Operator.<VideoTags>perform((dao) -> {
			QueryBuilder<VideoTags, Integer> qb = dao.queryBuilder()
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			qb.where().eq(VideoTags.COL_VIDEO, video);

			for (VideoTags vt : qb.query()) {
				retval.add(vt.getTag());
			}
		}, VideoTags.class);
		return(retval);
	}
}
