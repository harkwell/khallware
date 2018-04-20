// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Date;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Comments.  A Comment is a user note attached to a blog.
 *
 * @author khall
 */
@DatabaseTable(tableName = Comment.TABLE)
public class Comment extends AtomEntity
{
	private static final Logger logger = LoggerFactory.getLogger(
		Comment.class);
	public static final String TABLE = "comments";
	public static final String COL_BLOG = "blog_id";

	public static final class Builder
			extends APIEntity.Builder<Builder, Comment>
	{
		public Builder(Comment comment)
		{
			super(comment);
			entity.modified = new Date();
		}

		public Builder content(String content)
		{
			entity.content = content;
			return(this);
		}

		public Builder desc(String desc)
		{
			entity.description = desc;
			return(this);
		}

		public Builder blog(Blog blog)
		{
			entity.blog = blog;
			return(this);
		}

		public Comment build()
		{
			return(entity);
		}
	}

	@DatabaseField private String content = null;
	@DatabaseField private String description = null;
	@DatabaseField(foreign = true, columnName = COL_BLOG,
		foreignAutoRefresh = true) private Blog blog;

	// This default constructor is required by jackson ObjectMapper!!!!!
	// The getters are also required for proper deserialization!!!
	public Comment() {}

	@Override
	public void preSave()
	{
		super.preSave();
		logger.trace("presaving comment");
		content = Util.sanitizePostedHTML(getContent());
	}

	public static Builder builder()
	{
		return(new Builder(new Comment()));
	}

	public String getTitle()
	{
		return(getDescription());
	}

	public String getDescription()
	{
		return(description);
	}

	public String getContent()
	{
		return((content == null) ? "" : content);
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public Blog getBlog()
	{
		return(blog);
	}

	@Override
	public int hashCode()
	{
		return(getId());
	}

	@Override
	public boolean equals(Object obj)
	{
		boolean retval = true;
		retval &= (obj != null);
		retval &= (retval && obj.getClass() == this.getClass());

		if (this.hashCode() != UNKNOWN && obj.hashCode() != UNKNOWN) {
			retval &= (this.hashCode() == obj.hashCode());
		}
		else {
			retval &= (this.getContent().equals(
				((Comment)obj).getContent()));
			retval &= (this.getDescription().equals(
				((Comment)obj).getDescription()));
		}
		return(retval);
	}

	@Override
	public String toString()
	{
		return(new StringBuilder()
			.append(super.toString()+", ")
			.append("description=\""+description+"\"")
			.toString());
	}
}
