// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

// SELECT *, CAST(disabled AS UNSIGNED) AS disabled FROM credentials;

/**
 * Credentials.  A Credentials represents a system user.
 *
 * @author khall
 */
@DatabaseTable(tableName = Credentials.TABLE)
public class Credentials
{
	private static final Logger logger = LoggerFactory.getLogger(
		Credentials.class);
	public static final String TABLE = "credentials";
	public static final String COL_REGIKEY = "regikey";
	public static final String COL_GROUP = "_group";
	public static final String COL_USER = "username";
	public static final String COL_ID = "id";
	public static final int UNKNOWN = -1;

	@DatabaseField @JsonIgnore private String password = null;
	@DatabaseField private String username = null;
	@DatabaseField private String email = null;
	@DatabaseField private String regikey = null;
	@DatabaseField private boolean disabled = true;
	@DatabaseField(foreign = true, foreignAutoRefresh = true,
		columnName = "_group") private Group group = null;
	@DatabaseField(generatedId = true, columnName = COL_ID)
	private int id = UNKNOWN;
	private long quota = UNKNOWN;

	// This default constructor is required by jackson ObjectMapper!!!!!
	// The getters are also required for proper deserialization!!!
	public Credentials() {}

	public Credentials(String username, String password, Group group)
	{
		this.username = username;
		this.password = password;
		this.group = group;
	}

	public Credentials(String username, String password, Group group,
			String email, String regikey, boolean disabled)
	{
		this.username = username;
		this.password = password;
		this.disabled = disabled;
		this.regikey = regikey;
		this.group = group;
		this.email = email;
	}

	public int getId()
	{
		return(id);
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getUsername()
	{
		return((username == null) ? "" : username);
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	@JsonIgnore
	public String getPassword()
	{
		return((password == null) ? "" : password);
	}

	public void setGroup(Group group)
	{
		this.group = group;
	}

	public Group getGroup()
	{
		return(group);
	}

	public String getEmail()
	{
		return((email == null) ? "" : email);
	}

	public void setRegikey(String regikey)
	{
		this.regikey = regikey;
	}

	public String getRegikey()
	{
		return((regikey == null) ? "" : regikey);
	}

	public void setQuota(long quota)
	{
		this.quota = quota;
	}

	public long getQuota()
	{
		return(quota);
	}

	public void disable(boolean val)
	{
		disabled = val;
	}

	public boolean isDisabled()
	{
		return(disabled);
	}

	@Override
	public boolean equals(Object obj)
	{
		boolean retval = true;
		retval &= (obj != null);
		retval &= (retval && obj.getClass() == this.getClass());
		retval &= (retval && this.getUsername().equals(
			((Credentials)obj).getUsername()));
		retval &= (retval && this.getPassword().equals(
			((Credentials)obj).getPassword()));
		return(retval);
	}

	// to pacify pmd
	@Override
	public int hashCode()
	{
		return(super.hashCode());
	}

	@Override
	public String toString()
	{
		return(new StringBuilder()
			.append((id != UNKNOWN) ? "id=\""+id+"\", " : "")
			.append("username=\""+getUsername()+"\", ")
			.append("password=\""+getPassword()+"\", ")
			.append("quota=\""+getQuota()+"\", ")
			.append("group={"+group+"}")
			.append("]")
			.toString());
	}
}
