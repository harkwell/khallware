// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.dstore;

import com.khallware.api.Datastore;
import com.khallware.api.DatastoreException;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.APIEntity;
import com.khallware.api.domain.Entity;
import com.khallware.api.domain.Group;
import com.j256.ormlite.stmt.Where;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class APICrudChain<T extends Entity> extends CrudChain<T>
{
	private static final Logger logger = LoggerFactory.getLogger(
		APICrudChain.class);

	public static Where filterOnMode(Where retval, Credentials creds)
	{
		try {
			if (creds != null) {
				filterOnModeUnwrapped(retval, creds);
			}
			else {
				retval.raw("true");
			}
		}
		catch (Exception e) {
			logger.error(""+e, e);
		}
		return(retval);
	}

	protected static Where filterOnModeUnwrapped(Where retval,
			Credentials creds) throws SQLException,
			DatastoreException
	{
		List<Group> list = Datastore.DS().getGroups(creds);
		String maskColumn = APIEntity.COL_MASK;
		boolean amRoot = false;

		for (Group group : list) {
			if (group.getId() == Group.ROOT) {
				amRoot = true;
			}
		}
		if (amRoot) {
			list.clear();
			retval.raw("true");
		}
		for (Group group : list) {
			retval.or(
				retval.and(
					retval.eq(APIEntity.COL_GROUP,
						group.getId()),
					retval.raw("("+maskColumn+" % 100)"
						+" >= 40")
				),
				retval.and(
					retval.eq(APIEntity.COL_USER,
						creds.getId()),
					retval.ge(maskColumn, new Integer(400))
				),
				retval.raw("("+maskColumn+" % 10) >= 4")
			);
		}
		return(retval);
	}
}
