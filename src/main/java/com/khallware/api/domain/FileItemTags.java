// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = FileItemTags.TABLE)
public class FileItemTags
{
	public static final String TABLE = "fileitem_tags";
	public static final String COL_FILEITEM = "fileitem";
	public static final String COL_TAG = "tag";

	@DatabaseField(generatedId = true) private int id;
	@DatabaseField(foreign = true, columnName = COL_TAG) private Tag tag;
	@DatabaseField(foreign = true, foreignAutoRefresh = true,
		columnName = COL_FILEITEM) private FileItem fileitem;

	// This default constructor is required by ORMlite
	public FileItemTags() {}

	public FileItemTags(FileItem fileitem, Tag tag)
	{
		this.fileitem = fileitem;
		this.tag = tag;
	}

	public FileItem getFileItem()
	{
		return(fileitem);
	}

	public Tag getTag()
	{
		return(tag);
	}
}
