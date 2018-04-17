// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = BlogTags.TABLE)
public class BlogTags
{
	public static final String TABLE = "blog_tags";
	public static final String COL_BLOG = "blog";
	public static final String COL_TAG = "tag";

	@DatabaseField(generatedId = true) private int id;
	@DatabaseField(foreign = true, columnName = COL_TAG) private Tag tag;
	@DatabaseField(foreign = true, foreignAutoRefresh = true,
		columnName = COL_BLOG) private Blog blog;

	// This default constructor is required by ORMlite
	public BlogTags() {}

	public BlogTags(Blog blog, Tag tag)
	{
		this.blog = blog;
		this.tag = tag;
	}

	public Blog getBlog()
	{
		return(blog);
	}

	public Tag getTag()
	{
		return(tag);
	}
}
