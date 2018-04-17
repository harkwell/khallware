// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.validation;

import com.khallware.api.domain.Video;
import com.khallware.api.domain.APIEntity;
import com.khallware.api.DatastoreException;
import com.khallware.api.Datastore;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This validator enforces the uniqueness of a video in the system.
 *
 * @author khall
 */
public class UniqueVideo extends FileUniqueness
{
	private static final Logger logger = LoggerFactory.getLogger(
		UniqueVideo.class);

	public UniqueVideo() {}

	public UniqueVideo(Uniqueness.DuplicateHandler handler)
	{
		setDuplicateHandler(handler);
	}

	@Override
	public List<APIEntity> readAPIEntities(String md5sum)
			throws DatastoreException
	{
		List<APIEntity> retval = new ArrayList<>();
		Video pattern = new Video();
		pattern.setMd5sum(md5sum);
		retval.addAll(Datastore.DS().find(pattern));
		return(retval);
	}

	@Override
	public File onRequestPostedFile(APIEntity entity)
	{
		return(new File(((Video)entity).getPath()));
	}
}
