// Copyright Kevin D.Hall 2014-2015

package com.khallware.api;

import com.khallware.api.domain.Location;
import java.util.Collection;
import java.util.List;
import java.util.Arrays;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class LocationFactoryTest
{
	private static final Logger logger = LoggerFactory.getLogger(
		LocationFactoryTest.class);

	private String input = null;
	private int size = -1;

	public LocationFactoryTest(String input, int size)
	{
		this.input = input;
		this.size = size;
	}

	@Parameterized.Parameters
	public static Collection locations()
	{
		String location1[] = { "pulaski", "Red Horse Tavern",
			"36.988720,-80.755756" };
		String location2[] = { "hotel", "Hotel Contessa",
			"29.423037,-98.490040" };
		String location3[] = { "basel", "Bildungszentrum 21 (hotel)",
			"47.558871,7.577541" };
		String location4[] = { "obx", "Outer Banks/Nags Head",
			"36.088376,-75.704379" };
		String location5[] = { "Mcafee Knob", "Roanoke County Virginia",
			"37.392472,-80.036472" };
		String bad1[] = { null, null, null };
		String bad2[] = { "foo", "foobar", null };
		return Arrays.asList(new Object[][] {
			{ simpleKml(location1), 1 },
			{ simpleKml(location2), 1 },
			{ simpleKml(location3), 1 },
			{ simpleKml(location4), 1 },
			{ simpleKml(location5), 1 },
			{ location(location1), 1 },
			{ location(location2), 1 },
			{ location(location3), 1 },
			{ location(location4), 1 },
			{ location(location5), 1 },
			{ simpleKml(bad1), 0 },
			{ simpleKml(bad2), 0 },
			{ location(bad1), 0 },
			{ location(bad2), 0 },
			{ "this is invalid kml", 0 },
			{ location(location1, location2), 2 },
			{ location(location1, location3, bad1), 2 },
			{ location(location1, bad2, location3), 2 },
			{ location(location1, location2, location3), 3 }
		});
	}

	@Test
	public void factoryTest()
	{
		try {
			List<Location> rslt = LocationFactory.make(input);
			logger.info("input ({}), output ({})", input, rslt);
			logger.info("created {} of {} locations", rslt.size(),
				size);
			assertTrue(rslt.size() == size);
		}
		catch (Exception e) {
			logger.error(""+e, e);
		}
	}

	protected static String simpleKml(String[]... locations)
	{
		StringBuilder retval = new StringBuilder()
			.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
			.append("<kml xmlns=\"http://www.opengis.net/kml/")
			.append("2.2\">")
			.append("\n<Document>\n");

		for (String[] location : locations) {
			retval.append("\t<Placemark>\n")
				.append((location[0] == null)
					? ""
					: "\t\t<name>"+location[0]+"</name>")
				.append((location[1] == null)
					? ""
					: "\t\t<description>"+location[1]
						+"</description>")
				.append("\t\t<Point>\n")
				.append((location[2] == null)
					? ""
					: "\t\t\t<coordinates>"+location[2]
						+",0</coordinates>")
				.append("\t\t</Point>\n")
				.append("\t</Placemark>\n");
		}
		return(""+(retval.append("</Document>\n").append("</kml>\n")));
	}

	protected static String location(String[]... locations)
	{
		StringBuilder retval = new StringBuilder()
			.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
			.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\"")
			.append(" xmlns:gx=\"http://www.google.com/kml/ext/")
			.append("2.2\" xmlns:kml=\"http://www.opengis.net/")
			.append("kml/2.2\" xmlns:atom=\"http://www.w3.org/")
			.append("2005\" /Atom\">\n")
			.append("<Document>\n")
			.append("\t<name>some name</name>\n")
			.append("\t<Folder>\n")
			.append("\t\t<name>folder name</name>\n")
			.append("\t\t<open>1</open>\n");

		for (String[] location : locations) {
			String[] xy = (""+location[2]).split(",");
			retval.append("\t\t<Placemark>\n")
				.append((location[0] == null)
					? ""
					: "\t\t\t<name>"+location[0]
						+"</name>\n")
				.append("\t\t\t<LookAt>\n")
				.append((location[2] == null)
					? ""
					: "\t\t\t\t<longitude>"+xy[1]
						+"</longitude>\n")
				.append((location[2] == null)
					? ""
					: "\t\t\t\t<latitude>"+xy[0]
						+"</latitude>\n")
				.append((location[2] == null)
					? ""
					: "\t\t\t\t<altitude>0</altitude>\n")
				.append((location[2] == null)
					? ""
					: "\t\t\t\t<heading>0</heading>\n")
				.append((location[2] == null)
					? ""
					: "\t\t\t\t<tilt>0</tilt>\n")
				.append((location[2] == null)
					? ""
					: "\t\t\t\t<range>0</range>\n")
				.append((location[2] == null)
					? ""
					: "\t\t\t\t<gx:altitudeMode>"
						+"relativeToSeaFloor"
						+"</gx:altitudeMode>\n")
				.append("\t\t\t</LookAt>\n")
				.append("\t\t\t<Point>\n")
				.append("\t\t\t\t<gx:drawOrder>1"
					+"</gx:drawOrder>\n")
				.append((location[2] == null)
					? ""
					: "\t\t\t\t<coordinates>"
						+location[2]
						+",0</coordinates>\n")
				.append("\t\t\t</Point>\n")
				.append("\t\t<Placemark>\n");
		}
		retval.append("\t</Folder>\n");
		retval.append("</Document>\n");
		retval.append("</kml>\n");
		return(""+retval);
	}
}
