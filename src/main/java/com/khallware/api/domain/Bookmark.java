// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.domain;

import com.khallware.api.enums.Rating;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URL;
import java.util.Date;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Bookmarks.  A Bookmark is a web URL with attributes.
 *
 * @author khall
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@DatabaseTable(tableName = Bookmark.TABLE)
public class Bookmark extends AtomEntity
{
	private static final Logger logger = LoggerFactory.getLogger(
		Bookmark.class);
	public static final String TABLE = "bookmarks";
	public static final String DEFENC = "UTF-8";
	public static final String COL_ID = "id";

	public static final class Builder
			extends APIEntity.Builder<Builder, Bookmark>
	{
		public Builder(Bookmark bookmark)
		{
			super(bookmark);
			entity.modified = new Date();
		}

		public Builder rating(Rating rating)
		{
			entity.rating = rating;
			return(this);
		}

		public Builder rating(String rating)
		{
			try {
				entity.rating = Rating.valueOf(rating);
			}
			catch (IllegalArgumentException e) {
				logger.trace(""+e, e);
				logger.warn("{}",""+e);
			}
			return(this);
		}

		public Builder url(URL url)
		{
			entity.url = ""+url;
			return(this);
		}

		public Builder url(String url)
		{
			URL retval = null;
			try {
				retval = new URL(url);
			}
			catch (MalformedURLException e) {
				logger.trace(""+e, e);
				logger.warn("{}",""+e);
			}
			return(url(retval));
		}

		public Builder name(String name)
		{
			entity.name = name;
			return(this);
		}

		public Bookmark build()
		{
			return(entity);
		}
	}

	@DatabaseField(defaultValue="average")
	private Rating rating = Rating.average;
	@DatabaseField private String name = null;
	@DatabaseField private String url = null;

	// This default constructor is required by jackson ObjectMapper!!!!!
	// The getters are also required for proper deserialization!!!
	public Bookmark() {}

	@Override
	public void preSave()
	{
		try {
			if (url != null) {
				new URL(URLDecoder.decode(url, DEFENC));
			}
		}
		catch (MalformedURLException|UnsupportedEncodingException e) {
			logger.error(""+e, e);
			url = null;
		}
	}

	@Override
	public URL getAtomURL()
	{
		URL retval = super.getAtomURL();
		try {
			if (url != null) {
				retval = new URL(URLDecoder.decode(url,DEFENC));
			}
		}
		catch (MalformedURLException|UnsupportedEncodingException e) {
			logger.error(""+e, e);
		}
		return(retval);
	}

	public static Builder builder()
	{
		return(new Builder(new Bookmark()));
	}

	public String getTitle()
	{
		return(getName());
	}

	public String getDescription()
	{
		return(getName());
	}

	public Rating getRating()
	{
		return(rating);
	}

	public void setRating(Rating rating)
	{
		this.rating = rating;
	}

	public String getName()
	{
		return(name);
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getURL()
	{
		return(url);
	}

	public void setURL(String url)
	{
		this.url = url;
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
			retval &= (this.getURL().equals(
				((Bookmark)obj).getURL()));
		}
		return(retval);
	}

	@Override
	public String toString()
	{
		return(new StringBuilder()
			.append(super.toString()+", ")
			.append("name=\""+name+"\", ")
			.append("rating=\""+rating+"\"")
			.toString());
	}
}
