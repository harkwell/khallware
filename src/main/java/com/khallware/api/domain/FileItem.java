// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.domain;

import com.khallware.api.Util;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.File;
import java.util.Date;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * The FileItems.  A FileItem represents any file maintained by the system.
 *
 * @author khall
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@DatabaseTable(tableName = FileItem.TABLE)
public class FileItem extends AtomEntity
{
	private static final Logger logger = LoggerFactory.getLogger(
		FileItem.class);
	public static final String TABLE = "fileitems";

	public static final class Builder
			extends APIEntity.Builder<Builder, FileItem>
	{
		public Builder(FileItem fileitem)
		{
			super(fileitem);
			entity.modified = new Date();
		}

		public Builder file(File file)
		{
			int idx = file.getName().lastIndexOf(".");
			name(file.getName());
			path(""+file);
			ext(file.getName().substring(idx));
			try {
				md5sum(Util.produceHashSum("MD5", file));
			}
			catch (Exception e) {
				logger.debug(""+e, e);
				logger.warn(""+e);
			}
			return(this);
		}

		public Builder name(String name)
		{
			entity.name = name;
			return(this);
		}

		public Builder mime(String mime)
		{
			entity.mime = mime;
			return(this);
		}

		public Builder ext(String ext)
		{
			entity.ext = ext;
			return(this);
		}

		public Builder path(String path)
		{
			entity.path = path;
			return(this);
		}

		public Builder md5sum(String md5sum)
		{
			entity.md5sum = md5sum;
			return(this);
		}

		public Builder desc(String desc)
		{
			entity.description = desc;
			return(this);
		}

		public FileItem build()
		{
			return(entity);
		}
	}

	@DatabaseField private String name = null;
	@DatabaseField private String mime = null;
	@DatabaseField private String ext = null;
	@DatabaseField private String path = null;
	@DatabaseField private String md5sum = null;
	@DatabaseField private String description = null;

	// This default constructor is required by jackson ObjectMapper!!!!!
	// The getters are also required for proper deserialization!!!
	public FileItem() {}

	@Override
	public String getFileExtension()
	{
		return(ext);
	}

	public static Builder builder()
	{
		return(new Builder(new FileItem()));
	}

	public String getTitle()
	{
		return(getName());
	}

	public String getDescription()
	{
		return(getName());
	}

	public String getName()
	{
		return(name);
	}

	public String getMime()
	{
		return(mime);
	}

	public String getExt()
	{
		return(ext);
	}

	public String getPath()
	{
		return(path);
	}

	public String getMd5sum()
	{
		return(md5sum);
	}

	public void setMd5sum(String md5sum)
	{
		this.md5sum = md5sum;
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
		retval &= (retval && this.hashCode() == obj.hashCode());
		return(retval);
	}

	@Override
	public String toString()
	{
		return(new StringBuilder()
			.append(super.toString()+", ")
			.append("name=\""+name+"\", ")
			.append("mime=\""+mime+"\", ")
			.append("path=\""+path+"\"")
			.toString());
	}
}
