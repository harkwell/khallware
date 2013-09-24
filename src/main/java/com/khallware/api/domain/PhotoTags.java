// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = PhotoTags.TABLE)
public class PhotoTags
{
	public static final String TABLE = "photo_tags";
	public static final String COL_PHOTO = "photo";
	public static final String COL_TAG = "tag";

	@DatabaseField(generatedId = true) private int id;
	@DatabaseField(foreign = true, columnName = COL_TAG) private Tag tag;
	@DatabaseField(foreign = true, foreignAutoRefresh = true,
		columnName = COL_PHOTO) private Photo photo;

	// This default constructor is required by ORMlite
	public PhotoTags() {}

	public PhotoTags(Photo photo, Tag tag)
	{
		this.photo = photo;
		this.tag = tag;
	}

	public Photo getPhoto()
	{
		return(photo);
	}

	public Tag getTag()
	{
		return(tag);
	}
}
