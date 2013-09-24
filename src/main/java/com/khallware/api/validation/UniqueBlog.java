// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.validation;

import com.khallware.api.domain.Blog;
import com.khallware.api.domain.APIEntity;
import com.khallware.api.DatastoreException;
import com.khallware.api.Datastore;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This validator enforces the uniqueness of a blog in the system.
 *
 * @author khall
 */
public class UniqueBlog extends Uniqueness
{
	private static final Logger logger = LoggerFactory.getLogger(
		UniqueBlog.class);

	public UniqueBlog() {}

	public UniqueBlog(Uniqueness.DuplicateHandler handler)
	{
		setDuplicateHandler(handler);
	}

	@Override
	public APIEntity readAPIEntity(long id) throws DatastoreException
	{
		return(Datastore.DS().getBlog((int)id));
	}

	@Override
	public List<APIEntity> readAPIEntities(APIEntity pattern)
			throws DatastoreException
	{
		List<APIEntity> retval = new ArrayList<>();
		retval.addAll(Datastore.DS().find(pattern));
		return(retval);
	}

	@Override
	public List<APIEntity> makeClosePatterns(APIEntity entity)
	{
		List<APIEntity> retval = new ArrayList<>();
		Blog blog = (Blog)entity;
		Blog pattern = new Blog();
		pattern.setDescription(blog.getDescription());
		pattern.setContent(blog.getContent());
		retval.add(pattern);
		return(retval);
	}
}
