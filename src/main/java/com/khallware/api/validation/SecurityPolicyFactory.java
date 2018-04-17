// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.validation;

import com.khallware.api.Util;
import com.khallware.api.Datastore;
import com.khallware.api.DatastoreException;
import com.khallware.api.ctrl.Security.Policy;
import com.khallware.api.domain.Credentials;
import nl.captcha.Captcha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SecurityPolicyFactory.  Create Policy instances given various items.
 *
 * @author khall
 */
public class SecurityPolicyFactory
{
	public static final String BAD_CAPTCHA = "invalid captcha";
	public static final String BAD_USERNAME = "invalid username: "
		+"must not currently exist, contain spaces or use capitals";
	public static final String PASS_MISMATCH = "passwords must match";

	public static Policy<String> captcha(final Captcha captcha)
	{
		return((String token) -> {
			if (!captcha.isCorrect(token)) {
				throw new PolicyViolation(BAD_CAPTCHA);
			}
		});
	}

	public static Policy<Credentials> username()
	{
		return((Credentials creds) -> {
			boolean fails = false;
			String username = creds.getUsername();
			Credentials dup = null;
			try {
				dup = Datastore.DS().getCredentials(username);
			}
			catch (DatastoreException e) {
				throw new PolicyViolation(e);
			}
			fails |= username.matches("^.*\\p{Upper}.*$");
			fails |= username.matches("^.*\\s.*$");

			if (dup != null) {
				fails |= creds.getUsername().equals(
					dup.getUsername());
			}
			if (fails) {
				throw new PolicyViolation(BAD_USERNAME);
			}
		});
	}

	public static Policy<Credentials> password(final String verify)
	{
		return((Credentials creds) -> {
			try {
				String pass = Util.hash(verify);

				if (!creds.getPassword().equals(pass)) {
					throw new PolicyViolation(
						PASS_MISMATCH);
				}
			}
			catch (Exception e) {
				throw new PolicyViolation(e);
			}
		});
	}
}
