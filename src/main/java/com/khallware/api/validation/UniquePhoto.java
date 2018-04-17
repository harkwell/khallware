// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.validation;

import com.khallware.api.domain.Photo;
import com.khallware.api.domain.APIEntity;
import com.khallware.api.DatastoreException;
import com.khallware.api.Datastore;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This validator enforces the uniqueness of a photo in the system.
 *
 * @author khall
 */
public class UniquePhoto extends FileUniqueness
{
	private static final Logger logger = LoggerFactory.getLogger(
		UniquePhoto.class);

	public UniquePhoto() {}

	public UniquePhoto(Uniqueness.DuplicateHandler handler)
	{
		setDuplicateHandler(handler);
	}

	@Override
	public List<APIEntity> readAPIEntities(String md5sum)
			throws DatastoreException
	{
		List<APIEntity> retval = new ArrayList<>();
		Photo pattern = new Photo();
		pattern.setMd5sum(md5sum);
		retval.addAll(Datastore.DS().find(pattern));
		return(retval);
	}

	@Override
	public File onRequestPostedFile(APIEntity entity)
	{
		return(new File(((Photo)entity).getPath()));
	}
}
