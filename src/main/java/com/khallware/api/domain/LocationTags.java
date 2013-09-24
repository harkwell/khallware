// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = LocationTags.TABLE)
public class LocationTags
{
	public static final String TABLE = "location_tags";
	public static final String COL_LOCATION = "location";
	public static final String COL_TAG = "tag";

	@DatabaseField(generatedId = true) private int id;
	@DatabaseField(foreign = true, columnName = COL_TAG) private Tag tag;
	@DatabaseField(foreign = true, foreignAutoRefresh = true,
		columnName = COL_LOCATION) private Location location;

	// This default constructor is required by ORMlite
	public LocationTags() {}

	public LocationTags(Location location, Tag tag)
	{
		this.location = location;
		this.tag = tag;
	}

	public Location getLocation()
	{
		return(location);
	}

	public Tag getTag()
	{
		return(tag);
	}
}
