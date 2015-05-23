// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.dstore;

import com.khallware.api.Datastore.Wrapper;
import com.khallware.api.DatastoreException;
import com.khallware.api.dstore.Operator.Operation;
import com.khallware.api.domain.BookmarkTags;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.Tag;
import com.khallware.api.domain.Bookmark;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.dao.Dao;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DBookmarks extends APICrudChain<Bookmark>
{
	private static final Logger logger = LoggerFactory.getLogger(
		DBookmarks.class);

	@Override
	public Class onGetClass()
	{
		return(Bookmark.class);
	}

	@Override
	public Bookmark onCreate(final Bookmark bookmark)
			throws DatastoreException
	{
		Operator.<Bookmark>perform((dao) -> {
			bookmark.preSave();
			dao.createOrUpdate(bookmark);
		}, Bookmark.class);
		return(bookmark);
	}

	@Override
	public Bookmark onRead(final long id) throws DatastoreException
	{
		final Wrapper<Bookmark> retval = new Wrapper<>();
		Operator.<Bookmark>perform((dao) -> {
			retval.item = dao.queryForId((int)id);
		}, Bookmark.class);
		return(retval.item);
	}

	@Override
	public List<Bookmark> onRead(final Pagination pg,
			final Credentials creds, final Bookmark pattern)
			throws DatastoreException
	{
		final Wrapper<List<Bookmark>> retval = new Wrapper<>();
		Operator.<Bookmark>perform((dao) -> {
			QueryBuilder<Bookmark, Integer> qb = dao.queryBuilder()
				.setCountOf(pg.returnCount())
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			retval.item = new ArrayList<Bookmark>();
			filterOnMode(qb.where(), creds);
			retval.count = pg.returnCount() ? qb.countOf() : -1;
			retval.item.addAll((pattern == null)
				? qb.query()
				: dao.queryForMatchingArgs(pattern));
		}, Bookmark.class);
		pg.setCount(retval.count);
		return(retval.item);
	}

	@Override
	public List<Bookmark> onRead(final Tag tag, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		final List<Bookmark> retval = new ArrayList<>();
		Operator.<BookmarkTags>perform((dao) -> {
			QueryBuilder<BookmarkTags, Integer> qb =
				dao.queryBuilder()
					.setCountOf(pg.returnCount())
					.offset(pg.calcCursorIndex())
					.limit(pg.getPageSize());
			qb.where().eq(BookmarkTags.COL_TAG, tag);

			if (!pg.returnCount()) {
				for (BookmarkTags bt : qb.query()) {
					retval.add(bt.getBookmark());
				}
			}
			pg.setCount(pg.returnCount() ? qb.countOf() : -1);
		}, BookmarkTags.class);
		return(retval);
	}

	@Override
	public Bookmark onUpdate(final Bookmark bookmark)
			throws DatastoreException
	{
		Operator.<Bookmark>perform((dao) -> {
			dao.update(bookmark);
		}, Bookmark.class);
		return(bookmark);
	}

	@Override
	public Bookmark onDelete(final Bookmark bookmark)
			throws DatastoreException
	{
		Operator.<Bookmark>perform((dao) -> {
			dao.delete(bookmark);
		}, Bookmark.class);
		return(bookmark);
	}

	@Override
	public Bookmark onAdd2Tag(final Bookmark bookmark, final Tag tag)
			throws DatastoreException
	{
		Operator.<BookmarkTags>perform((dao) -> {
			dao.createOrUpdate(new BookmarkTags(bookmark, tag));
		}, BookmarkTags.class);
		return(bookmark);
	}

	@Override
	public Bookmark onDeleteFromTags(final Bookmark bookmark, final Tag tag)
			throws DatastoreException
	{
		Operator.<BookmarkTags>perform((dao) -> {
			dao.delete(new BookmarkTags(bookmark, tag));
		}, BookmarkTags.class);
		return(bookmark);
	}

	@Override
	public List<Tag> onGetTags(final Bookmark bookmark, final Pagination pg)
			throws DatastoreException
	{
		final List<Tag> retval = new ArrayList<>();
		Operator.<BookmarkTags>perform((dao) -> {
			QueryBuilder<BookmarkTags, Integer> qb =
				dao.queryBuilder()
					.offset(pg.calcCursorIndex())
					.limit(pg.getPageSize());
			qb.where().eq(BookmarkTags.COL_BOOKMARK, bookmark);

			for (BookmarkTags bt : qb.query()) {
				retval.add(bt.getTag());
			}
		}, BookmarkTags.class);
		return(retval);
	}
}
