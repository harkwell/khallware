// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = ContactTags.TABLE)
public class ContactTags
{
	public static final String TABLE = "contact_tags";
	public static final String COL_BLOG = "contact";
	public static final String COL_TAG = "tag";

	@DatabaseField(generatedId = true) private int id;
	@DatabaseField(foreign = true, columnName = COL_TAG) private Tag tag;
	@DatabaseField(foreign = true, foreignAutoRefresh = true,
		columnName = COL_BLOG) private Contact contact;

	// This default constructor is required by ORMlite
	public ContactTags() {}

	public ContactTags(Contact contact, Tag tag)
	{
		this.contact = contact;
		this.tag = tag;
	}

	public Contact getContact()
	{
		return(contact);
	}

	public Tag getTag()
	{
		return(tag);
	}
}
