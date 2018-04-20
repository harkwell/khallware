// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.validation;

import com.khallware.api.Datastore;
import com.khallware.api.DatastoreException;
import com.khallware.api.APIException;
import com.khallware.api.domain.Group;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This validator enforces the uniqueness of a group in the system.
 *
 * @author khall
 */
public class UniqueGroup extends APIValidator<Group>
{
	private static final Logger logger = LoggerFactory.getLogger(
		UniqueGroup.class);

	public void enforce(Group group) throws APIException
	{
		logger.trace("enforcing group: {}",group);
		try {
			enforceUnwrapped(group);
		}
		catch (DatastoreException e) {
			throw new DuplicateException(e);
		}
	}

	protected void enforceUnwrapped(Group group) throws APIException,
			DatastoreException
	{
		Group pattern = new Group(group.getName(), (String)null);
		String errorMsg1 = "group must not be root";
		String errorMsg2 = "groups must be unique";

		if (group.getId() == Group.ROOT) {
			throw new DuplicateException(errorMsg1);
		}
		else if (group.getId() != Group.UNKNOWN) {
			if (Datastore.DS().getGroup(group.getId()) != null) {
				throw new DuplicateException(errorMsg2);
			}
		}
		else {
			List<String> list = new ArrayList<>();

			for (Group found : Datastore.DS().getGroups(pattern)) {
				list.add(""+found);
			}
			if (!list.isEmpty()) {
				throw new DuplicateException(
					errorMsg2+" : "+list);
			}
		}
	}
}
