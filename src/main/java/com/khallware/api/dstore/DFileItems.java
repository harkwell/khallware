// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.dstore;

import com.khallware.api.Datastore.Wrapper;
import com.khallware.api.DatastoreException;
import com.khallware.api.dstore.Operator.Operation;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.FileItemTags;
import com.khallware.api.domain.Tag;
import com.khallware.api.domain.FileItem;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.dao.Dao;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DFileItems extends APICrudChain<FileItem>
{
	private static final Logger logger = LoggerFactory.getLogger(
		DFileItems.class);

	@Override
	public Class onGetClass()
	{
		return(FileItem.class);
	}

	@Override
	public FileItem onCreate(final FileItem fileitem)
			throws DatastoreException
	{
		Operator.<FileItem>perform((dao) -> {
			fileitem.preSave();
			dao.createOrUpdate(fileitem);
		}, FileItem.class);
		return(fileitem);
	}

	@Override
	public FileItem onRead(final long id) throws DatastoreException
	{
		final Wrapper<FileItem> retval = new Wrapper<>();
		Operator.<FileItem>perform((dao) -> {
			retval.item = dao.queryForId((int)id);
		}, FileItem.class);
		return(retval.item);
	}

	@Override
	public List<FileItem> onRead(final Pagination pg,
			final Credentials creds, final FileItem pattern)
			throws DatastoreException
	{
		final Wrapper<List<FileItem>> retval = new Wrapper<>();
		Operator.<FileItem>perform((dao) -> {
			QueryBuilder<FileItem, Integer> qb = dao.queryBuilder()
				.setCountOf(pg.returnCount())
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			retval.item = new ArrayList<FileItem>();
			filterOnMode(qb.where(), creds);
			retval.count = pg.returnCount() ? qb.countOf() : -1;
			retval.item.addAll((pattern == null)
				? qb.query()
				: dao.queryForMatchingArgs(pattern));
		}, FileItem.class);
		pg.setCount(retval.count);
		return(retval.item);
	}

	@Override
	public List<FileItem> onRead(final Tag tag, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		final List<FileItem> retval = new ArrayList<>();
		Operator.<FileItemTags>perform((dao) -> {
			QueryBuilder<FileItemTags, Integer> qb =
				dao.queryBuilder()
					.setCountOf(pg.returnCount())
					.offset(pg.calcCursorIndex())
					.limit(pg.getPageSize());
			qb.where().eq(FileItemTags.COL_TAG, tag);

			if (!pg.returnCount()) {
				for (FileItemTags ft : qb.query()) {
					retval.add(ft.getFileItem());
				}
			}
			pg.setCount(pg.returnCount() ? qb.countOf() : -1);
		}, FileItemTags.class);
		return(retval);
	}

	@Override
	public FileItem onUpdate(final FileItem fileitem)
			throws DatastoreException
	{
		Operator.<FileItem>perform((dao) -> dao.update(fileitem),
			FileItem.class);
		return(fileitem);
	}

	@Override
	public FileItem onDelete(final FileItem fileitem)
			throws DatastoreException
	{
		Operator.<FileItem>perform((dao) -> dao.delete(fileitem),
			FileItem.class);
		return(fileitem);
	}

	@Override
	public FileItem onAdd2Tag(final FileItem fileitem, final Tag tag)
			throws DatastoreException
	{
		Operator.<FileItemTags>perform((dao) -> {
			dao.createOrUpdate(new FileItemTags(fileitem, tag));
		}, FileItemTags.class);
		return(fileitem);
	}

	@Override
	public FileItem onDeleteFromTags(final FileItem fileitem, final Tag tag)
			throws DatastoreException
	{
		Operator.<FileItemTags>perform((dao) -> {
			dao.delete(new FileItemTags(fileitem, tag));
		}, FileItemTags.class);
		return(fileitem);
	}

	@Override
	public List<Tag> onGetTags(final FileItem fileitem, final Pagination pg)
			throws DatastoreException
	{
		final List<Tag> retval = new ArrayList<>();
		Operator.<FileItemTags>perform((dao) -> {
			QueryBuilder<FileItemTags, Integer> qb =
				dao.queryBuilder()
					.offset(pg.calcCursorIndex())
					.limit(pg.getPageSize());
			qb.where().eq(FileItemTags.COL_FILEITEM, fileitem);

			for (FileItemTags ft : qb.query()) {
				retval.add(ft.getTag());
			}
		}, FileItemTags.class);
		return(retval);
	}
}
