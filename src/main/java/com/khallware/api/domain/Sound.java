// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.domain;

import com.khallware.api.Util;
import com.khallware.api.enums.Genre;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import adamb.vorbis.VorbisCommentHeader;
import adamb.vorbis.VorbisIO;
import adamb.vorbis.CommentField;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.File;
import java.util.Date;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * The Sound.  A Sound is a container for managing system music, audio and other
 * audial components.
 *
 * @author khall
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@DatabaseTable(tableName = Sound.TABLE)
public class Sound extends AtomEntity
{
	private static final Logger logger = LoggerFactory.getLogger(
		Sound.class);
	public static final String TABLE = "sounds";

	public static final class Builder
			extends APIEntity.Builder<Builder, Sound>
	{
		public Builder(Sound sound)
		{
			super(sound);
			entity.modified = new Date();
		}

		public Builder file(File file) throws Exception
		{
			VorbisCommentHeader header = null;
			header = VorbisIO.readComments(file);
			md5sum(Util.produceHashSum("MD5", file));
			name(file.getName());
			path(""+file);

			for (CommentField field : header.fields) {
				switch (field.name.toLowerCase()) {
				case "title":
					title(field.value);
					break;
				case "artist":
					artist(field.value);
					break;
				case "genre":
					genre(field.value);
					break;
				case "album":
					album(field.value);
					break;
				case "date":
					recording(field.value);
					break;
				case "organization":
					publisher(field.value);
					break;
				case "version":
					break;
				default:
					logger.warn("unhandled ogg vorbis file "
						+"comment field \"{}\"",
						field.name);
					break;
				}
			}
			return(this);
		}

		public Builder title(String title)
		{
			entity.title = title;
			return(this);
		}

		public Builder artist(String artist)
		{
			entity.artist = artist;
			return(this);
		}

		public Builder genre(String genre)
		{
			return(genre(Genre.valueOf(genre)));
		}

		public Builder genre(Genre genre)
		{
			entity.genre = genre;
			return(this);
		}

		public Builder album(String album)
		{
			entity.album = album;
			return(this);
		}

		public Builder recording(String recording)
		{
			entity.recording = dateFromString(recording);
			return(this);
		}

		public Builder recording(Date recording)
		{
			entity.recording = recording;
			return(this);
		}

		public Builder publisher(String publisher)
		{
			entity.publisher = publisher;
			return(this);
		}

		public Builder name(String name)
		{
			entity.name = name;
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

		public Sound build()
		{
			return(entity);
		}
	}

	@DatabaseField private String name = null;
	@DatabaseField private String path = null;
	@DatabaseField private String description = null;
	@DatabaseField private String title = null;
	@DatabaseField private String album = null;
	@DatabaseField private String artist = null;
	@DatabaseField private String md5sum = null;
	@DatabaseField private Genre genre = null;
	@DatabaseField private Date recording = null;
	@DatabaseField private String publisher = null;

	// This default constructor is required by jackson ObjectMapper!!!!!
	// The getters are also required for proper deserialization!!!
	public Sound() {}

	@Override
	public String getFileExtension()
	{
		return("ogg");
	}

	public static Builder builder()
	{
		return(new Builder(new Sound()));
	}

	public String getDescription()
	{
		return(getName());
	}

	public String getName()
	{
		return(name);
	}

	public String getPath()
	{
		return(path);
	}

	public String getTitle()
	{
		return(title);
	}

	public String getArtist()
	{
		return(artist);
	}

	public Genre getGenre()
	{
		return(genre);
	}

	public String getPublisher()
	{
		return(publisher);
	}

	public Date getRecording()
	{
		return(recording);
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

		if (this.hashCode() != UNKNOWN && obj.hashCode() != UNKNOWN) {
			retval &= (this.hashCode() == obj.hashCode());
		}
		else {
			retval &= (this.getMd5sum().equals(
				((Sound)obj).getMd5sum()));
		}
		return(retval);
	}

	@Override
	public String toString()
	{
		return(new StringBuilder()
			.append(super.toString()+", ")
			.append("name=\""+name+"\", ")
			.append("artist=\""+artist+"\", ")
			.append("genre=\""+genre+"\", ")
			.append("album=\""+album+"\", ")
			.append("publisher=\""+publisher+"\", ")
			.append("path=\""+path+"\"")
			.toString());
	}
}
