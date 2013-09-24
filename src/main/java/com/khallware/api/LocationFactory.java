// Copyright Kevin D.Hall 2014-2015

package com.khallware.api;

import com.khallware.api.domain.Location;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Make Location instances.
 *
 * @author khall
 */
public class LocationFactory
{
	private static final Logger logger = LoggerFactory.getLogger(
		LocationFactory.class);
	public static final int MAX_IS_READLIMIT = (1024 * 1024); // 1Mb

	/**
	 * Create a list of Location instances given a serialized kml doc.
	 *
	 * @param kml - serialized kml doc.
	 * @return List<Location> - list of locations
	 */
	public static List<Location> make(String kml) throws MalformedEntity
	{
		try {
			InputStream is = new ByteArrayInputStream(
				kml.getBytes());
			return(make(is));
		}
		catch (Exception e) {
			throw new MalformedEntity(e);
		}
	}

	/**
	 * Create a list of Location instances given a kml InputStream.
	 *
	 * @param is - kml InputStream.
	 * @return List<Location> - list of locations
	 */
	public static List<Location> make(InputStream is) throws MalformedEntity
	{
		List<Location> retval = new ArrayList<>();
		try {
			is.mark(MAX_IS_READLIMIT);
			retval = make(Kml.unmarshal(is));
		}
		catch (Exception e1) {
			try {
				is.reset();
				retval = makeSimple(is);
			}
			catch (IOException e2) {
				throw new MalformedEntity(e2);
			}
		}
		return(retval);
	}

	/**
	 * Create a list of Location instances given a simple kml InputStream.
	 *
	 * @param is - simple kml InputStream.
	 * @return List<Location> - list of locations
	 */
	public static List<Location> makeSimple(InputStream is)
			throws MalformedEntity
	{
		try {
			DocumentBuilderFactory factory =
				DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			return(make(builder.parse(is)));
		}
		catch (Exception e) {
			throw new MalformedEntity(e);
		}
	}

	/**
	 * Create a list of Location instances given a Kml instance.
	 *
	 * @param is - kml instance.
	 * @return List<Location> - list of locations
	 */
	public static List<Location> make(Kml kml)
	{
		return((kml.getFeature() == null)
			? new ArrayList<Location>()
			: make(kml.getFeature()));
	}

	/**
	 * Create a list of Location instances given a Feature instance.
	 *
	 * @param feature - Feature instance.
	 * @return List<Location> - list of locations
	 */
	public static List<Location> make(Feature feature)
	{
		List<Location> retval = new ArrayList<>();
		List<Location> rslt = null;

		if (feature instanceof Document) {
			for (Feature f : ((Document)feature).getFeature()) {
				retval.addAll(make(f));
			}
		}
		else if (feature instanceof Folder) {
			for (Feature f : ((Folder)feature).getFeature()) {
				retval.addAll(make(f));
			}
		}
		else if (feature instanceof Placemark) {
			rslt = make(((Placemark)feature).getGeometry());

			for (Location loc : rslt) {
				loc.setName(feature.getName());
				loc.setAddress(feature.getAddress());
				loc.setDescription(feature.getDescription());
				logger.trace("found location ({})",loc);
				retval.add(loc);
			}
		}
		else {
			logger.trace("skip, uninteresting feature {}", feature);
		}
		return(retval);
	}

	protected static List<Coordinate> getCoordinates(Polygon polygon)
	{
		List<Coordinate> retval = new ArrayList<>();
		Boundary boundary = polygon.getOuterBoundaryIs();
		LinearRing linearRing = (boundary != null)
			? boundary.getLinearRing()
			: null;

		if (linearRing != null) {
			retval = linearRing.getCoordinates();
		}
		else {
			logger.warn("no linear ring or boundary in polygon");
		}
		return(retval);
	}

	/**
	 * Create a list of Location instances given a Geometry instance.
	 *
	 * @param geometry - Geometry instance.
	 * @return List<Location> - list of locations
	 */
	public static List<Location> make(Geometry geometry)
	{
		List<Location> retval = new ArrayList<>();

		if (geometry instanceof Polygon) {
			retval.addAll(make(getCoordinates((Polygon)geometry)));
		}
		if (geometry instanceof Point) {
			retval.addAll(make(((Point)geometry).getCoordinates()));
		}
		else {
			logger.warn("unhandled geometry item ({})", geometry);
		}
		return(retval);
	}

	/**
	 * Create a list of Location instances given a list of Coordinate
	 * instances.
	 *
	 * @param list - list of Geometry instances.
	 * @return List<Location> - list of locations
	 */
	public static List<Location> make(List<Coordinate> list)
	{
		List<Location> retval = new ArrayList<>();
		Location location = null;
		logger.trace("given {} coordinates", list.size());

		for (Coordinate coordinate : list) {
			if ((location = make(coordinate)) != null) {
				retval.add(location);
			}
		}
		return(retval);
	}

	/**
	 * Create a list of Location instances given a Coordinate instance.
	 *
	 * @param coordinate - Coordinate instances
	 * @return Location - location
	 */
	public static Location make(Coordinate coordinate)
	{
		Location retval = null;

		if (coordinate != null) {
			retval = new Location();
			retval.setLatitude(""+coordinate.getLatitude());
			retval.setLongitude(""+coordinate.getLongitude());
		}
		return(retval);
	}

	/**
	 * Create a list of Location instances given a simple XML Document.
	 *
	 * @param doc - simple XML doc
	 * @return List<Location> - list of locations
	 */
	public static List<Location> make(org.w3c.dom.Document doc)
	{
		List<Location> retval = new ArrayList<>();
		NodeList placemarks = doc.getElementsByTagName("Placemark");
		NodeList places = doc.getElementsByTagName("Placemark");
		doc.getDocumentElement().normalize();

		for (int idx=0; idx < placemarks.getLength(); idx++) {
			Node node = placemarks.item(idx);

			switch (node.getNodeType()) {
			case Node.ELEMENT_NODE:
				Location location = new Location();
				String token = get("coordinates",(Element)node);
				String[] latlon = token.split(",");
				location.setName(get("name", (Element)node));
				location.setDescription(get("description",
					(Element)node));

				if (latlon.length == 2) {
					location.setLatitude(latlon[0]);
					location.setLongitude(latlon[1]);
				}
				retval.add(location);
			}
		}
		return(retval);
	}

	protected static String get(String item, Element element)
	{
		Node node = element.getElementsByTagName(item).item(0);
		String content = (node == null) ? "" : node.getTextContent();
		return((content == null) ? "" : content);
	}

	public static String makeKML(Location... locations)
	{
		return(makeKML(Arrays.asList(locations)));
	}

	/**
	 * Create an XML representation based on the list of Location instances.
	 *
	 * @param locations - list of Location instances.
	 * @return String - XML representation
	 */
	public static String makeKML(List<Location> locations)
	{
		StringBuilder kml = new StringBuilder();
		kml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		kml.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\" ");
		kml.append("xmlns:gx=\"http://www.google.com/kml/ext/2.2\" ");
		kml.append("xmlns:kml=\"http://www.opengis.net/kml/2.2\" ");
		kml.append("xmlns:atom=\"http://www.w3.org/2005/Atom\">\n");
		kml.append("<Folder>\n");

		for (Location location : locations) {
			kml.append("\t\t<Placemark>\n");
			kml.append("\t\t\t<name>");
			kml.append(location.getName());
			kml.append("</name>\n");
			kml.append("\t\t\t<description>");
			kml.append(location.getDescription());
			kml.append("</description>\n");
			kml.append("\t\t\t<Point>\n");
			kml.append("\t\t\t\t<coordinates>");
			kml.append(location.getLongitude()+",");
			kml.append(location.getLatitude());
			kml.append("</coordinates>\n");
			kml.append("\t\t\t</Point>\n");
			kml.append("\t\t</Placemark>\n");
		}
		kml.append("\t</Folder>\n");
		kml.append("</kml>");
		return(kml.toString());
	}
}
