// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.validation;

import com.khallware.api.APIException;
import com.khallware.api.DatastoreException;
import com.khallware.api.domain.APIEntity;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This validator enforces the uniqueness of an entity in the system.
 *
 * @author khall
 */
public class Uniqueness extends APIValidator<APIEntity>
{
	public static final String ERROR_MSG = "duplicate entity error";
	private static final Logger logger = LoggerFactory.getLogger(
		Uniqueness.class);
	protected DuplicateHandler handler = null;

	public static interface DuplicateHandler
	{
		public void handle(APIEntity duplicate);
	}

	public void setDuplicateHandler(DuplicateHandler handler)
	{
		this.handler = handler;
	}

	public boolean hasDuplicateHandler()
	{
		return(handler != null);
	}

	public APIEntity readAPIEntity(long id) throws DatastoreException
	{
		return(null);
	}

	public List<APIEntity> readAPIEntities(APIEntity pattern)
			throws DatastoreException
	{
		List<APIEntity> retval = new ArrayList<>();
		return(retval);
	}

	public List<APIEntity> makeClosePatterns(APIEntity entity)
	{
		List<APIEntity> retval = new ArrayList<>();
		return(retval);
	}

	public List<APIEntity> onGetCloseMatches(APIEntity pattern)
			throws DatastoreException
	{
		List<APIEntity> retval = new ArrayList<>();
		APIEntity found = null;
		long id = pattern.getId();

		if (id != APIEntity.UNKNOWN) {
			if ((found = readAPIEntity(id)) != null) {
				retval.add(found);
			}
		}
		else {
			for (APIEntity p : makeClosePatterns(pattern)) {
				retval.addAll(readAPIEntities(p));
			}
		}
		logger.trace("{} matches for ({}) found",retval.size(),pattern);
		return(retval);
	}

	public void enforce(APIEntity criteria) throws APIException
	{
		try {
			for (APIEntity match : onGetCloseMatches(criteria)) {
				if (criteria.equals(match)) {
					if (hasDuplicateHandler()) {
						handler.handle(criteria);
					}
					throw new DuplicateException(ERROR_MSG);
				}
				logger.trace("MISMATCH ({}) and ({})", criteria,
					match);
			}
		}
		catch (DatastoreException e) {
			throw new DuplicateException(e);
		}
	}
}
