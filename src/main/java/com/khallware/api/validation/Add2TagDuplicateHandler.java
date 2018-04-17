// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.validation;

import com.khallware.api.validation.Uniqueness.DuplicateHandler;
import com.khallware.api.ctrl.CrudController;
import com.khallware.api.DatastoreException;
import com.khallware.api.Datastore;
import com.khallware.api.domain.APIEntity;
import com.khallware.api.domain.Tag;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * When an entity is added to a tag, there is likely a chance that an entity tag
 * already exists. This validator enforces this rule.
 *
 * @author khall
 */
public class Add2TagDuplicateHandler implements DuplicateHandler
{
	private static final Logger logger = LoggerFactory.getLogger(
		Add2TagDuplicateHandler.class);
	private long tagId = Tag.ROOT;

	public Add2TagDuplicateHandler(long tagId)
	{
		this.tagId = tagId;
	}

	public void handle(APIEntity entity)
	{
		try {
			Datastore.DS().add2Tag(entity, tagId);
		}
		catch (DatastoreException e) {
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
	}
}
