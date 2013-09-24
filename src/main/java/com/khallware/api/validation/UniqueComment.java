// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.validation;

import com.khallware.api.domain.Comment;
import com.khallware.api.domain.APIEntity;
import com.khallware.api.DatastoreException;
import com.khallware.api.Datastore;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This validator enforces the uniqueness of a comment in the system.
 *
 * @author khall
 */
public class UniqueComment extends Uniqueness
{
	private static final Logger logger = LoggerFactory.getLogger(
		UniqueComment.class);

	public UniqueComment() {}

	public UniqueComment(Uniqueness.DuplicateHandler handler)
	{
		setDuplicateHandler(handler);
	}

	@Override
	public APIEntity readAPIEntity(long id) throws DatastoreException
	{
		return(Datastore.DS().getComment((int)id));
	}

	@Override
	public List<APIEntity> readAPIEntities(APIEntity pattern)
			throws DatastoreException
	{
		List<APIEntity> retval = new ArrayList<>();
		retval.addAll(Datastore.DS().findComments((Comment)pattern));
		return(retval);
	}

	@Override
	public List<APIEntity> makeClosePatterns(APIEntity entity)
	{
		List<APIEntity> retval = new ArrayList<>();
		Comment comment = (Comment)entity;
		Comment pattern = new Comment();
		pattern.setContent(comment.getContent());
		retval.add(pattern);
		return(retval);
	}
}
