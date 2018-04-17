// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.validation;

import com.khallware.api.APIException;
import com.khallware.api.DatastoreException;
import com.khallware.api.domain.APIEntity;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This validator enforces that specific fields exist for an APIEntity and
 * they are non empty.
 *
 * @author khall
 */
public class Completeness extends APIValidator<APIEntity>
{
	public static final String ERROR_MSG = "missing required values";
	private static final Logger logger = LoggerFactory.getLogger(
		Completeness.class);

	public List<String> onGetFieldNames(APIEntity e)
	{
		return(new ArrayList<String>());
	}

	public List<Field> onGetRequiredFields(APIEntity e) throws APIException
	{
		List<Field> retval = new ArrayList<>();
		Field field = null;
		try {
			for (String name : onGetFieldNames(e)) {
				field = e.getClass().getDeclaredField(name);
				field.setAccessible(true);
				retval.add(field);
			}
		}
		catch (Exception exception) {
			throw new IncompleteException(exception);
		}
		return(retval);
	}

	public void enforce(APIEntity entity) throws APIException
	{
		List<String> missing = new ArrayList<>();
		try {
			for (Field field : onGetRequiredFields(entity)) {
				if (field.get(entity) == null) {
					missing.add(field.getName());
				}
			}
		}
		catch (Exception e) {
			throw new IncompleteException(e);
		}
		if (missing.size() > 0) {
			throw new IncompleteException(ERROR_MSG+": "+missing);
		}
	}
}
