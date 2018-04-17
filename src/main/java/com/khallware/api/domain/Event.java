// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Event.  An Event represents an activity in time.
 *
 * @author khall
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@DatabaseTable(tableName = Event.TABLE)
public class Event extends AtomEntity
{
	private static final Logger logger = LoggerFactory.getLogger(
		Event.class);
	public static final String TABLE = "events";

	public static final class Builder
			extends APIEntity.Builder<Builder, Event>
	{
		public Builder(Event event)
		{
			super(event);
			entity.created = new Date();
			entity.modified = new Date();
			entity.start = new Date();
			entity.end = (entity.end == null)
				? entity.start
				: entity.end;
		}

		public Builder duration(long duration)
		{
			entity.duration = duration;
			return(this);
		}

		public Builder created(Date created)
		{
			entity.created = created;
			return(this);
		}

		public Builder created(String created)
		{
			created((created == null)
				? (Date)null
				: makeDate(created));
			return(this);
		}

		public Builder start(Date start)
		{
			entity.start = start;
			return(this);
		}

		public Builder start(String start)
		{
			start((start == null)
				? (Date)null
				: makeDate(start));
			return(this);
		}

		public Builder end(Date end)
		{
			entity.end = end;
			return(this);
		}

		public Builder end(String end)
		{
			end((end == null)
				? (Date)null
				: makeDate(end));
			return(this);
		}

		public Builder uid(String uid)
		{
			entity.uid = uid;
			return(this);
		}

		public Builder name(String name)
		{
			entity.name = name;
			return(this);
		}

		public Builder ics(String ics)
		{
			entity.ics = ics;
			return(this);
		}

		public Builder description(String description)
		{
			entity.description = description;
			return(this);
		}

		public Event build()
		{
			return(entity);
		}

		private Date makeDate(String date, String fmt)
		{
			Date retval = null;
			try {
				retval = new SimpleDateFormat(fmt).parse(date);
			}
			catch (ParseException e) {
				logger.trace(""+e, e);
				logger.warn("unconventional date: "+e);
			}
			return(retval);
		}

		private Date makeDate(String date)
		{
			Date retval = null;
			String[] formats = { DATE_FORMAT,
				"EEE MMM d HH:mm:ss z yyyy",
				"yyyyMMdd'T'HHmmssZ" };

			for (String format : formats) {
				if ((retval = makeDate(date, format)) != null) {
					break;
				}
			}
			return(retval);
		}
	}

	@DatabaseField private long duration = 0;
	@DatabaseField private Date created = null;
	@DatabaseField private Date start = null;
	@DatabaseField private Date end = null;
	@DatabaseField private String ics = "";
	@DatabaseField private String name = "";
	@DatabaseField private String uid = null;
	@DatabaseField private String description = null;

	// This default constructor is required by jackson ObjectMapper!!!!!
	// The getters are also required for proper deserialization!!!
	public Event() {}

	@Override
	public String getFileExtension()
	{
		return("ics");
	}

	public static Builder builder()
	{
		return(new Builder(new Event()));
	}

	public String getTitle()
	{
		return(getName());
	}

	public String getUID()
	{
		return(uid);
	}

	public void setUID(String uid)
	{
		if (uid != null && !uid.isEmpty()) {
			this.uid = ""+UUID.fromString(uid);
		}
	}

	public String getName()
	{
		return(name);
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return(description);
	}

	public void setIcs(String ics)
	{
		this.ics = ics;
	}

	public String getIcs()
	{
		return((ics == null) ? "" : ics);
	}

	public long getDuration()
	{
		return(duration);
	}

	public void setDuration(Date start)
	{
		this.duration = duration;
	}

	public void setStart(Date start)
	{
		this.start = start;
	}

	public Date getStart()
	{
		start = (start == null) ? new Date() : start;
		return(start);
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
			retval &= (this.getUID().equals(((Event)obj).getUID()));
		}
		return(retval);
	}

	@Override
	public String toString()
	{
		return(new StringBuilder()
			.append(super.toString()+", ")
			.append("name=\""+getName()+"\", ")
			.append("description=\""+getDescription()+"\", ")
			.append("ics_len="+getIcs().length()+" bytes, ")
			.append("start=\""+getStart()+"\"")
			.toString());
	}
}
