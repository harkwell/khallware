// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.validation;

import com.khallware.api.domain.Contact;
import com.khallware.api.APIException;
import java.util.Arrays;
import java.util.List;

/**
 * This validator enforces that specific fields exist for a contact entity and
 * they are non empty.
 *
 * @author khall
 */
public class CompleteContact extends APIValidator<Contact>
{
	public void enforce(Contact contact) throws APIException
	{
		boolean pass = false;
		pass |= basicConstitution(contact);

		if (!pass) {
			throw new IncompleteException("contact incomplete");
		}
	}

	protected boolean basicConstitution(Contact contact)
	{
		boolean retval = false;
		boolean hasName = notNullOrEmpty(contact.getName());
		boolean hasVcard = notNullOrEmpty(contact.getVcard());
		boolean hasOneOther = notNullOrEmpty(contact.getDescription());
		hasOneOther |= notNullOrEmpty(contact.getEmail());
		hasOneOther |= notNullOrEmpty(contact.getPhone());
		hasOneOther |= notNullOrEmpty(contact.getAddress());
		hasOneOther |= notNullOrEmpty(contact.getOrganization());
		retval = (hasVcard || (hasName && hasOneOther));
		return(retval);
	}
}
