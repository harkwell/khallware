// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * The Session.  A Session is a container for managing system connections.
 *
 * @author khall
 */
@DatabaseTable(tableName = Session.TABLE)
public class Session extends APIEntity
{
	private static final Logger logger = LoggerFactory.getLogger(
		Session.class);
	public static final long DEF_TIMEOUT = (30 * 60 * 1000); // 30 minutes
	public static final String TABLE = "sessions";
	public static final String COL_CREDS = "credential_id";
	public static final String COL_NAME = "name";

	@DatabaseField private String name = "";
	@DatabaseField(foreign = true, columnName = COL_CREDS,
		foreignAutoRefresh = true) private Credentials credentials;

	// This default constructor is required by jackson ObjectMapper!!!!!
	// The getters are also required for proper deserialization!!!
	public Session() {}

	public Session(int id, Credentials credentials, String name)
	{
		this.id = id;
		this.name = name;
		this.credentials = credentials;
	}

	@Override
	public void preSave()
	{
		super.preSave();

		if (credentials != null) {
			this.user = credentials.getId();
			this.group = credentials.getGroup();
			logger.trace("preSave() finished: ({})", this);
		}
		else {
			throw new IllegalStateException("missing credentials");
		}
	}

	public String getName()
	{
		return(name);
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Credentials getCredentials()
	{
		return(credentials);
	}

	public void setCredentials(Credentials credentials)
	{
		this.credentials = credentials;
	}

	public boolean isValid(long timeout)
	{
		boolean retval = true;
		Date expiration = new Date(getModified().getTime() + timeout);

		if (new Date().after(expiration)) {
			retval = false;
		}
		return(retval);
	}

	public boolean isValid()
	{
		return(isValid(DEF_TIMEOUT));
	}

	@Override
	public int hashCode()
	{
		return(getId());
	}

	@Override
	public boolean equals(Object obj)
	{
		boolean retval = true;
		retval &= (obj != null);
		retval &= (retval && obj.getClass() == this.getClass());
		retval &= (retval && this.hashCode() == obj.hashCode());
		return(retval);
	}

	@Override
	public String toString()
	{
		return(new StringBuilder()
			.append(super.toString()+", ")
			.append("name=\""+getName()+"\", ")
			.append("credentials=("+getCredentials()+")")
			.toString());
	}
}
