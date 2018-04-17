// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.validation;

import com.khallware.api.APIException;
import com.khallware.api.domain.Event;
import java.util.Arrays;
import java.util.List;

/**
 * This validator enforces that specific fields exist for an Event and
 * they are non empty.
 *
 * @author khall
 */
public class CompleteEvent extends APIValidator<Event>
{
	public void enforce(Event event) throws APIException
	{
		boolean pass = false;
		pass |= basicConstitution(event);

		if (!pass) {
			throw new IncompleteException("event incomplete");
		}
	}

	protected boolean basicConstitution(Event event)
	{
		boolean retval = false;
		boolean hasName = notNullOrEmpty(event.getName());
		boolean hasOneOther = notNullOrEmpty(event.getDescription());
		hasOneOther |= (event.getDuration() > 0);
		hasOneOther |= notNullOrEmpty(event.getStart());
		retval = (hasName && hasOneOther);
		return(retval);
	}
}
