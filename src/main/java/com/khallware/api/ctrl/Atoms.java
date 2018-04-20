// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.ctrl;

import com.khallware.api.dstore.Pagination;
import com.khallware.api.DatastoreException;
import com.khallware.api.Datastore;
import com.khallware.api.APIException;
import com.khallware.api.domain.AtomEntity;
import com.khallware.api.domain.Credentials;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/atoms")
public class Atoms
{
	private static final Logger logger = LoggerFactory.getLogger(
		Atoms.class);

	/**
	 * List atoms via file extension.
	 */
	@GET
	@Path("/{service}.atom")
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public Response handleExtGet(@Context HttpServletRequest request,
		@PathParam(value="service") String service)
	{
		return(handleGet(request, service));
	}

	/**
	 * List atoms.
	 */
	@GET
	@Path("/{service}")
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public Response handleGet(@Context HttpServletRequest request,
		@PathParam(value="service") String service)
	{
		Response retval = null;
		String sitename = Datastore.DS().getProperty("sitename");
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n");
		sb.append("<feed xmlns=\"http://www.w3.org/2005/Atom\">\n");
		sb.append("\t<title>New \"");
		sb.append(service.toLowerCase());
		sb.append("\" postings to "+sitename+"</title>\n");
		sb.append("\t<updated>");
		sb.append(new Date().toString());
		sb.append("</updated>\n");
		sb.append("\t<author>\n");
		sb.append("\t\t<name>Kevin D.Hall</name>\n");
		sb.append("\t</author>\n");
		sb.append("\t<id>");
		sb.append(UUID.randomUUID());
		sb.append("</id>\n\n");
		try {
			List<AtomEntity> atoms = new ArrayList<>();
			String base = ""+request.getRequestURL();
			Credentials creds = Util.getCredentials(request);
			Pagination pg = new Pagination();
			base = base.substring(0, base.indexOf("/atoms"));

			switch (service.toLowerCase()) {
			case "tags":
				Util.lastPeriod(
					Datastore.DS().getTags(pg, creds),
					base, sb);
				break;
			case "bookmarks":
				Util.lastPeriod(
					Datastore.DS().getBookmarks(pg, creds),
					base, sb);
				break;
			case "locations":
				Util.lastPeriod(
					Datastore.DS().getLocations(pg, creds),
					base, sb);
				break;
			case "contacts":
				Util.lastPeriod(
					Datastore.DS().getContacts(pg, creds),
					base,sb);
				break;
			case "events":
				Util.lastPeriod(
					Datastore.DS().getEvents(pg, creds),
					base, sb);
				break;
			case "blogs":
				Util.lastPeriod(
					Datastore.DS().getBlogs(pg, creds),
					base, sb);
				break;
			case "photos":
				Util.lastPeriod(
					Datastore.DS().getPhotos(pg, creds),
					base, sb);
				break;
			case "sounds":
				Util.lastPeriod(
					Datastore.DS().getSounds(pg, creds),
					base, sb);
				break;
			case "videos":
				Util.lastPeriod(
					Datastore.DS().getVideos(pg, creds),
					base, sb);
				break;
			case "fileitems":
				Util.lastPeriod(
					Datastore.DS().getFileItems(pg, creds),
					base, sb);
				break;
			default:
				logger.error("unhandled svc \"{}\"",service);
				break;
			}
			sb.append("\n</feed>\n");
			retval = Response.status(200).entity(""+sb).build();
		}
		catch (DatastoreException|APIException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn("{}",e);
		}
		return(retval);
	}
}
