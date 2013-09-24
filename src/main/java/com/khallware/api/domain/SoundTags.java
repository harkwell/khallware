// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = SoundTags.TABLE)
public class SoundTags
{
	public static final String TABLE = "sound_tags";
	public static final String COL_SOUND = "sound";
	public static final String COL_TAG = "tag";

	@DatabaseField(generatedId = true) private int id;
	@DatabaseField(foreign = true, columnName = COL_TAG) private Tag tag;
	@DatabaseField(foreign = true, foreignAutoRefresh = true,
		columnName = COL_SOUND) private Sound sound;

	// This default constructor is required by ORMlite
	public SoundTags() {}

	public SoundTags(Sound sound, Tag tag)
	{
		this.sound = sound;
		this.tag = tag;
	}

	public Sound getSound()
	{
		return(sound);
	}

	public Tag getTag()
	{
		return(tag);
	}
}
