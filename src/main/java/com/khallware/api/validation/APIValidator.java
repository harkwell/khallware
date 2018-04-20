// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.validation;

import com.khallware.api.APIException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * The Validator abstract implementation.  This class implements much of what
 * a validator should accomplish.
 *
 * @author khall
 */
public abstract class APIValidator<T> implements Validator<T>
{
	private static final Logger logger = LoggerFactory.getLogger(
		APIValidator.class);

	public abstract void enforce(T criteria) throws APIException;

	public boolean isValid(T criteria)
	{
		boolean retval = true;
		try {
			enforce(criteria);
		}
		catch (APIException e) {
			logger.trace(""+e, e);
			logger.warn("{}",e);
			retval = false;
		}
		return(retval);
	}

	protected static boolean notNullOrEmpty(Object item)
	{
		return(item != null && !(""+item).isEmpty());
	}
}
