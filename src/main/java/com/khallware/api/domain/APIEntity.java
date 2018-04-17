// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.domain;

import com.khallware.api.enums.Mode;
import com.j256.ormlite.field.DatabaseField;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Entity implementation.  Most database entities will extend this base
 * class.
 *
 * @author khall
 */
public abstract class APIEntity implements Entity
{
	private static final Logger logger = LoggerFactory.getLogger(
		APIEntity.class);
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String COL_GROUP = "_group";
	public static final String COL_USER = "user";
	public static final String COL_MASK = "mask";
	public static final String COL_ID = "id";
	public static final int DEF_MASK = 700;
	public static final int UNKNOWN = 0;

	/**
	 * The Entity Builder.  This builder pattern is used to create
	 * instances of entities.
	 *
	 * @author khall
	 */
	public static abstract class Builder<T extends Builder,
			E extends APIEntity>
	{
		protected E entity = null;

		public Builder(E entity)
		{
			this.entity = entity;
			entity.id = UNKNOWN;
			entity.mask = DEF_MASK;
			entity.user = UNKNOWN;
			entity.modified = null;
		}

		public T id(int id)
		{
			entity.id = Math.max(id, UNKNOWN);
			return((T)this);
		}

		public T id(String id)
		{
			int val = UNKNOWN;
			try {
				val = Integer.parseInt(id);
			}
			catch (NumberFormatException e) {
				logger.trace(""+e, e);
				logger.warn(""+e);
			}
			return(id(val));
		}

		public T modified(Date modified)
		{
			entity.modified = modified;
			return((T)this);
		}

		public T modified(String modified)
		{
			return(modified(dateFromString(modified)));
		}

		protected Date dateFromString(String uncooked)
		{
			Date date = null;
			try {
				date = new SimpleDateFormat(DATE_FORMAT).parse(
					uncooked);
			}
			catch (ParseException e) {
				logger.trace(""+e, e);
				logger.warn(""+e);
			}
			return(date);
		}

		public T user(int user)
		{
			entity.user = user;
			return((T)this);
		}

		public T addMode(Mode mode)
		{
			entity.mask |= mode.mask();
			return((T)this);
		}

		public T delMode(Mode mode)
		{
			entity.mask &= (~ mode.mask());
			return((T)this);
		}

		public T mask(int mask)
		{
			entity.mask = mask;
			return((T)this);
		}
	}

	@DatabaseField(generatedId=true, columnName=COL_ID) protected int id;
	@DatabaseField(foreign = true, foreignAutoRefresh = true,
		columnName = COL_GROUP) protected Group group = null;
	@DatabaseField protected Date modified = null;
	@DatabaseField(columnName=COL_MASK) protected int mask = DEF_MASK;
	@DatabaseField(columnName=COL_USER) protected int user = UNKNOWN;

	/**
	 * This is called right before the entity is saved.  Override it to
	 * manipulate fields that should be updated pre-save.
	 */
	public void preSave()
	{
		this.modified = new Date();
		logger.debug("assigning new date to entity ({})", modified);
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return(id);
	}

	public void setUser(int user)
	{
		this.user = user;
	}

	public int getUser()
	{
		return(user);
	}

	public void setGroup(Group group)
	{
		this.group = group;
	}

	public Group getGroup()
	{
		return(group);
	}

	public void setMask(int mask)
	{
		this.mask = mask;
	}

	public int getMask()
	{
		return(mask);
	}

	public Date getModified()
	{
		modified = (modified == null) ? new Date() : modified;
		return(modified);
	}

	public synchronized UUID getUUID()
	{
		return(Util.makeUUID(this));
	}

	public String getDateAsString()
	{
		return(getDateAsString(getModified()));
	}

	public static String getDateAsString(Date date)
	{
		return(new SimpleDateFormat(DATE_FORMAT).format(date));
	}

	@Override
	public boolean equals(Object obj)
	{
		boolean retval = true;
		retval &= (obj != null);
		retval &= (retval && obj.getClass() == this.getClass());
		retval &= (retval && this.hashCode() == obj.hashCode());
		return(retval);
	}

	@Override
	public int hashCode()
	{
		return(getId());
	}

	@Override
	public String toString()
	{
		return(new StringBuilder()
			.append(this.getClass().getSimpleName()+" ")
			.append((id != UNKNOWN) ? "id=\""+id+"\", " : "")
			.append("user=\""+user+"\"")
			.append(", modified=\""+getDateAsString(getModified()))
			.append("\"")
			.toString());
	}
}
