// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Location.  A Location represents a geographic point on earth.
 *
 * @author khall
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@DatabaseTable(tableName = Location.TABLE)
public class Location extends AtomEntity
{
	private static final Logger logger = LoggerFactory.getLogger(
		Location.class);
	public static final String TABLE = "locations";

	@DatabaseField private String name = "";
	@DatabaseField private String address = null;
	@DatabaseField private String description = null;
	@DatabaseField private double latitude = 0.00;
	@DatabaseField private double longitude = 0.00;

	// This default constructor is required by jackson ObjectMapper!!!!!
	// The getters are also required for proper deserialization!!!
	public Location() {}

	@Override
	public String getFileExtension()
	{
		return("kml");
	}

	public void setTitle(String title)
	{
		setName(title);
	}

	public String getTitle()
	{
		return(getName());
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return(name);
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public String getAddress()
	{
		return(address);
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getDescription()
	{
		return(description);
	}

	public void setLatitude(String latitude)
	{
		this.latitude = Double.parseDouble(latitude);
	}

	public double getLatitude()
	{
		return(latitude);
	}

	public void setLongitude(String longitude)
	{
		this.longitude = Double.parseDouble(longitude);
	}

	public double getLongitude()
	{
		return(longitude);
	}

	@Override
	public int hashCode()
	{
		return(id);
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
			retval &= ((""+this.getLatitude()).equals(
				""+((Location)obj).getLatitude()));
			retval &= ((""+this.getLongitude()).equals(
				""+((Location)obj).getLongitude()));
		}
		return(retval);
	}

	@Override
	public String toString()
	{
		return(new StringBuilder()
			.append("loc:"+latitude+","+longitude+" [")
			.append(super.toString())
			.append("]")
			.toString());
	}
}
