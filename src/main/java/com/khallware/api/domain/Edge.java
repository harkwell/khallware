// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Edges.  An Edge represents a mathematical graph edge for the Group.
 *
 * @author khall
 */
@DatabaseTable(tableName = Edge.TABLE)
public class Edge
{
	public static final String TABLE = "edges";
	public static final String COL_GROUP = "_group";
	public static final String COL_PARENT = "parent";
	public static final int UNKNOWN = -1;

	@DatabaseField(foreign = true, foreignAutoRefresh = true,
		columnName = COL_GROUP) private Group group = null;
	@DatabaseField(foreign = true, foreignAutoRefresh = true,
		columnName = COL_PARENT) private Group parent = null;

	// This default constructor is required by jackson ObjectMapper!!!!!
	// The getters are also required for proper deserialization!!!
	public Edge() {}

	public Edge(Group group, Group parent)
	{
		this.group = group;
		this.parent = parent;
	}

	public Group getGroup()
	{
		return(group);
	}

	public void setGroup(Group group)
	{
		this.group = group;
	}

	public Group getParent()
	{
		return(parent);
	}

	public void setParent(Group parent)
	{
		this.parent = parent;
	}

	@Override
	public String toString()
	{
		return(new StringBuilder()
			.append("group=\""+getGroup()+"\", ")
			.append("parent=\""+getParent()+"\"")
			.toString());
	}
}
