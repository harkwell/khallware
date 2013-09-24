// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = EventTags.TABLE)
public class EventTags
{
	public static final String TABLE = "event_tags";
	public static final String COL_EVENT = "event";
	public static final String COL_TAG = "tag";

	@DatabaseField(generatedId = true) private int id;
	@DatabaseField(foreign = true, columnName = COL_TAG) private Tag tag;
	@DatabaseField(foreign = true, foreignAutoRefresh = true,
		columnName = COL_EVENT) private Event event;

	// This default constructor is required by ORMlite
	public EventTags() {}

	public EventTags(Event event, Tag tag)
	{
		this.event = event;
		this.tag = tag;
	}

	public Event getEvent()
	{
		return(event);
	}

	public Tag getTag()
	{
		return(tag);
	}
}
