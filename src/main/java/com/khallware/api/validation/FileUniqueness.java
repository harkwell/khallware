// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.validation;

import com.khallware.api.Util;
import com.khallware.api.APIException;
import com.khallware.api.domain.APIEntity;
import com.khallware.api.DatastoreException;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * This validator enforces there is only one file on the system with the given
 * md5sum.
 *
 * @author khall
 */
public class FileUniqueness extends Uniqueness
{
	public static final String ERROR_MSG = "file already posted";
	private static final Logger logger = LoggerFactory.getLogger(
		FileUniqueness.class);

	public List<APIEntity> readAPIEntities(String md5sum)
			throws DatastoreException
	{
		return(new ArrayList<APIEntity>());
	}

	public File onRequestPostedFile(APIEntity entity)
	{
		return(null);
	}

	@Override
	public void enforce(APIEntity criteria) throws APIException
	{
		logger.trace("enforcing {}",criteria);
		try {
			File posted = onRequestPostedFile(criteria);
			String md5sum = Util.produceHashSum("MD5", posted);
			boolean flag = false;

			for (APIEntity entity : readAPIEntities(md5sum)) {
				if (hasDuplicateHandler()) {
					handler.handle(entity);
				}
				flag = true;
			}
			if (flag) {
				throw new DuplicateException(
				 	ERROR_MSG+": "+posted);
			}
		}
		catch (Exception e) {
			throw new DuplicateException(e);
		}
	}
}
