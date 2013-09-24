// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.domain;

import com.khallware.api.APIException;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@JsonIgnoreProperties(ignoreUnknown = true)
@DatabaseTable(tableName = Blog.TABLE)
public class Blog extends AtomEntity
{
	private static final Logger logger = LoggerFactory.getLogger(
		Blog.class);
	public static final String TABLE = "blogs";

	public static final class Builder
			extends APIEntity.Builder<Builder, Blog>
	{
		public Builder(Blog blog)
		{
			super(blog);
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

		public Blog build()
		{
			return(entity);
		}
	}

	@DatabaseField private String content = null;
	@DatabaseField private String description = null;

	// This default constructor is required by jackson ObjectMapper!!!!!
	// The getters are also required for proper deserialization!!!
	public Blog() {}

	@Override
	public void updateUrl(String baseUrl) throws APIException
	{
		try {
			atomURL = new URL(baseUrl+"/../blog.html?id="+id);
		}
		catch (MalformedURLException e) {
			logger.error(""+e, e);
			throw new APIException(e);
		}
	}

	@Override
	public void preSave()
	{
		super.preSave();
		content = Util.sanitizePostedHTML(getContent());
	}

	public static Builder builder()
	{
		return(new Builder(new Blog()));
	}

	public String getTitle()
	{
		return(getDescription());
	}

	public String getDescription()
	{
		return(description);
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getContent()
	{
		return((content == null) ? "" : content);
	}

	public void setContent(String content)
	{
		this.content = content;
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
				((Blog)obj).getContent()));
			retval &= (this.getDescription().equals(
				((Blog)obj).getDescription()));
		}
		return(retval);
	}

	@Override
	public String toString()
	{
		return(new StringBuilder()
			.append(super.toString()+", ")
			.append("content="+getContent().length()+"bytes")
			.toString());
	}
}
