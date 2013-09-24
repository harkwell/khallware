// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = BookmarkTags.TABLE)
public class BookmarkTags
{
	public static final String TABLE = "bookmark_tags";
	public static final String COL_BOOKMARK = "bookmark";
	public static final String COL_TAG = "tag";

	@DatabaseField(generatedId = true) private int id;
	@DatabaseField(foreign = true, columnName = COL_TAG) private Tag tag;
	@DatabaseField(foreign = true, foreignAutoRefresh = true,
		columnName = COL_BOOKMARK) private Bookmark bookmark;

	// This default constructor is required by ORMlite
	public BookmarkTags() {}

	public BookmarkTags(Bookmark bookmark, Tag tag)
	{
		this.bookmark = bookmark;
		this.tag = tag;
	}

	public Bookmark getBookmark()
	{
		return(bookmark);
	}

	public Tag getTag()
	{
		return(tag);
	}
}
