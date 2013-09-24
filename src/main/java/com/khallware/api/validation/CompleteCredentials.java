// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.validation;

import com.khallware.api.domain.Credentials;
import com.khallware.api.APIException;
import java.util.Arrays;
import java.util.List;

/**
 * This validator enforces that specific fields exist for a Credentials and
 * they are non empty.
 *
 * @author khall
 */
public class CompleteCredentials extends APIValidator<Credentials>
{
	public void enforce(Credentials creds) throws APIException
	{
		boolean pass = false;
		pass |= basicConstitution(creds);

		if (!pass) {
			throw new IncompleteException("creds incomplete");
		}
	}

	protected boolean basicConstitution(Credentials creds)
	{
		boolean retval = false;
		boolean hasUsername = notNullOrEmpty(creds.getUsername());
		boolean hasPassword = notNullOrEmpty(creds.getPassword());
		boolean hasGroup = (creds.getGroup() != null);
		boolean hasEmail = notNullOrEmpty(creds.getEmail());
		retval = (hasUsername && hasPassword && hasGroup && hasEmail);
		return(retval);
	}
}
