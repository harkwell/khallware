// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.validation;

import com.khallware.api.domain.Credentials;
import com.khallware.api.DatastoreException;
import com.khallware.api.APIException;
import com.khallware.api.Datastore;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * This validator enforces the uniqueness of a credentials in the system.
 *
 * @author khall
 */
public class UniqueCredentials extends APIValidator<Credentials>
{
	private static final Logger logger = LoggerFactory.getLogger(
		UniqueCredentials.class);

	public void enforce(Credentials creds) throws APIException
	{
		try {
			logger.trace("enforcing creds: {}",creds);
			enforceUnwrapped(creds);
		}
		catch (DatastoreException e) {
			throw new DuplicateException(e);
		}
	}

	protected void enforceUnwrapped(Credentials creds) throws APIException,
			DatastoreException
	{
		// Credentials pattern = new Credentials();
		Credentials found = null;
		String errorMsg = "creds must be unique";

		if ((found = Datastore.DS().getCredentials(creds.getId()))
					!= null) {
			throw new DuplicateException(errorMsg+" -looks like: "
				+"\""+found+"\"");
		}
		else if ((found = Datastore.DS().getCredentials(
				creds.getUsername())) != null) {
			throw new DuplicateException(errorMsg+" -looks like: "
				+"\""+found+"\"");
		}
	}
}
