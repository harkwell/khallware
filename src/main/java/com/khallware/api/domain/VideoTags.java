// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = VideoTags.TABLE)
public class VideoTags
{
	public static final String TABLE = "video_tags";
	public static final String COL_VIDEO = "video";
	public static final String COL_TAG = "tag";

	@DatabaseField(generatedId = true) private int id;
	@DatabaseField(foreign = true, columnName = COL_TAG) private Tag tag;
	@DatabaseField(foreign = true, foreignAutoRefresh = true,
		columnName = COL_VIDEO) private Video video;

	// This default constructor is required by ORMlite
	public VideoTags() {}

	public VideoTags(Video video, Tag tag)
	{
		this.video = video;
		this.tag = tag;
	}

	public Video getVideo()
	{
		return(video);
	}

	public Tag getTag()
	{
		return(tag);
	}
}
