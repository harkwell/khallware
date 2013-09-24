// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * The Group.  A Group puts users into a discrete set.
 *
 * @author khall
 */
@DatabaseTable(tableName = Group.TABLE)
public class Group
{
	public static final int ROOT = 0;
	public static final int UNKNOWN = -1;
	public static final String TABLE = "groups";
	public static final String COL_ID = "id";
	public static final String COL_NAME = "name";
	public static final String COL_DISABLED = "disabled";

	@DatabaseField(generatedId=true, columnName=COL_ID) private int id;
	@DatabaseField(columnName=COL_NAME) private String name = "wheel";
	@DatabaseField(columnName=COL_DISABLED) private boolean disabled = true;
	@DatabaseField private String description = "root group";

	// This default constructor is required by jackson ObjectMapper!!!!!
	// The getters are also required for proper deserialization!!!
	public Group()
	{
		id = ROOT;
	}

	public Group(String name, String description)
	{
		id = UNKNOWN;
		this.name = name;
		this.description = description;
	}

	public Group(int id, String name, String description)
	{
		this.id = Math.max(id, (UNKNOWN + 1));
		this.name = name;
		this.description = description;
	}

	public String getDescription()
	{
		return(description);
	}

	public String getName()
	{
		return(name);
	}

	public int getId()
	{
		return(id);
	}

	public void setId(int id)
	{
		this.id = id;
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
			.append("name=\""+getName()+"\", ")
			.append("description=\""+getDescription()+"\"")
			.toString());
	}
}
