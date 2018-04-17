// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.dstore;

import com.khallware.api.Datastore.Wrapper;
import com.khallware.api.DatastoreException;
import com.khallware.api.dstore.Operator.Operation;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.BlogTags;
import com.khallware.api.domain.Tag;
import com.khallware.api.domain.Blog;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.dao.Dao;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DBlogs extends APICrudChain<Blog>
{
	private static final Logger logger = LoggerFactory.getLogger(
		DBlogs.class);

	@Override
	public Class onGetClass()
	{
		return(Blog.class);
	}

	@Override
	public Blog onCreate(final Blog blog)
			throws DatastoreException
	{
		Operator.<Blog>perform((dao) -> {
			blog.preSave();
			dao.createOrUpdate(blog);
		}, Blog.class);
		return(blog);
	}

	@Override
	public Blog onRead(final long id) throws DatastoreException
	{
		final Wrapper<Blog> retval = new Wrapper<>();
		Operator.<Blog>perform((dao) -> {
			retval.item = dao.queryForId((int)id);
		}, Blog.class);
		return(retval.item);
	}

	@Override
	public List<Blog> onRead(final Pagination pg,
			final Credentials creds, final Blog pattern)
			throws DatastoreException
	{
		final Wrapper<List<Blog>> retval = new Wrapper<>();
		Operator.<Blog>perform((dao) -> {
			QueryBuilder<Blog, Integer> qb = dao.queryBuilder()
				.setCountOf(pg.returnCount())
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			retval.item = new ArrayList<Blog>();
			filterOnMode(qb.where(), creds);
			retval.count = pg.returnCount() ? qb.countOf() : -1;
			retval.item.addAll((pattern == null)
				? qb.query()
				: dao.queryForMatchingArgs(pattern));
		}, Blog.class);
		pg.setCount(retval.count);
		return(retval.item);
	}

	@Override
	public List<Blog> onRead(final Tag tag, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		final List<Blog> retval = new ArrayList<>();
		Operator.<BlogTags>perform((dao) -> {
			QueryBuilder<BlogTags, Integer> qb = dao.queryBuilder()
				.setCountOf(pg.returnCount())
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			qb.where().eq(BlogTags.COL_TAG, tag);

			if (!pg.returnCount()) {
				for (BlogTags bt : qb.query()) {
					retval.add(bt.getBlog());
				}
			}
			pg.setCount(pg.returnCount() ? qb.countOf() : -1);
		}, BlogTags.class);
		return(retval);
	}

	@Override
	public Blog onUpdate(final Blog blog)
			throws DatastoreException
	{
		Operator.<Blog>perform((dao) -> dao.update(blog), Blog.class);
		return(blog);
	}

	@Override
	public Blog onDelete(final Blog blog)
			throws DatastoreException
	{
		Operator.<Blog>perform((dao) -> dao.delete(blog), Blog.class);
		return(blog);
	}

	@Override
	public Blog onAdd2Tag(final Blog blog, final Tag tag)
			throws DatastoreException
	{
		Operator.<BlogTags>perform((dao) -> {
			dao.createOrUpdate(new BlogTags(blog, tag));
		}, BlogTags.class);
		return(blog);
	}

	@Override
	public Blog onDeleteFromTags(final Blog blog, final Tag tag)
			throws DatastoreException
	{
		Operator.<BlogTags>perform((dao) -> {
			dao.delete(new BlogTags(blog, tag));
		}, BlogTags.class);
		return(blog);
	}

	public List<Tag> onGetTags(final Blog blog, final Pagination pg)
			throws DatastoreException
	{
		final List<Tag> retval = new ArrayList<>();
		Operator.<BlogTags>perform((dao) -> {
			QueryBuilder<BlogTags, Integer> qb = dao.queryBuilder()
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			qb.where().eq(BlogTags.COL_BLOG, blog);

			for (BlogTags bt : qb.query()) {
				retval.add(bt.getTag());
			}
		}, BlogTags.class);
		return(retval);
	}
}
