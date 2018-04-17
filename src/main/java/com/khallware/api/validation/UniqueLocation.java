// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.validation;

import com.khallware.api.domain.Location;
import com.khallware.api.domain.APIEntity;
import com.khallware.api.DatastoreException;
import com.khallware.api.Datastore;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This validator enforces the uniqueness of a location in the system.
 *
 * @author khall
 */
public class UniqueLocation extends Uniqueness
{
	private static final Logger logger = LoggerFactory.getLogger(
		UniqueLocation.class);

	public UniqueLocation() {}

	public UniqueLocation(Uniqueness.DuplicateHandler handler)
	{
		setDuplicateHandler(handler);
	}

	@Override
	public APIEntity readAPIEntity(long id) throws DatastoreException
	{
		return(Datastore.DS().getLocation((int)id));
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
		Location location = (Location)entity;
		Location pattern = new Location();
		pattern.setLatitude(""+location.getLatitude());
		pattern.setLongitude(""+location.getLongitude());
		retval.add(pattern);
		return(retval);
	}
}
