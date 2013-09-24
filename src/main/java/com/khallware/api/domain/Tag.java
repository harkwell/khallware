// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Date;

/**
 * The Tag.  A Tag is a container for managing collections of entities.
 *
 * @author khall
 */
@DatabaseTable(tableName = Tag.TABLE)
public class Tag extends AtomEntity
{
	public static final int ROOT = 0;
	public static final String TABLE = "tags";
	public static final String COL_NAME = "name";
	public static final String COL_PARENT = "parent";

	@DatabaseField(columnName = COL_NAME) private String name = "root";
	@DatabaseField(columnName = COL_PARENT) private int parent = ROOT;

	// This default constructor is required by jackson ObjectMapper!!!!!
	// The getters are also required for proper deserialization!!!
	public Tag()
	{
		id = ROOT;
		group = new Group();
		mask = 760;
	}

	public Tag(int id, int parent, String name, int user, Date modified)
	{
		this.id = Math.max(id, ROOT);
		this.parent = Math.max(id, ROOT);
		this.group = new Group();
		this.modified = modified;
		this.user = user;
		this.name = name;
	}

	public boolean isRoot()
	{
		return(id == ROOT);
	}

	public String getTitle()
	{
		return(getName());
	}

	public String getDescription()
	{
		return(getName());
	}

	public int getParent()
	{
		return(parent);
	}

	public String getName()
	{
		return(name);
	}

	@Override
	public String toString()
	{
		return(new StringBuilder()
			.append(super.toString()+", ")
			.append("parent=\""+getParent()+"\"")
			.toString());
	}
}
