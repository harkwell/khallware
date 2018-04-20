// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.ctrl;

import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.Location;
import com.khallware.api.domain.Tag;
import com.khallware.api.Datastore;
import com.khallware.api.APIException;
import com.khallware.api.Unauthorized;
import com.khallware.api.LocationFactory;
import com.khallware.api.MalformedEntity;
import com.khallware.api.DatastoreException;
import com.khallware.api.dstore.Pagination;
import com.khallware.api.validation.Validator;
import com.khallware.api.validation.UniqueLocation;
import com.khallware.api.validation.Add2TagDuplicateHandler;
import com.khallware.api.validation.CompleteAPIEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Path("/locations")
public class Locations extends CrudController<Location>
{
	public static final long MAX_KML_ELMNTS = 1024;
	private static final Logger logger = LoggerFactory.getLogger(
		Locations.class);

	/**
	 * Create a new location.
	 */
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response handlePost(@Context HttpServletRequest request,
			@QueryParam("tagId") @DefaultValue("0") int tagId,
			String json)
	{
		return(handlePost(Location.class, request, json, tagId,
			getValidators(tagId)));
	}

	/**
	 * Create a new location.
	 */
	@POST
	@Consumes("application/vnd.google-earth.kml+xml")
	@Produces("application/json")
	public Response handleKMLPost(@Context HttpServletRequest request,
			@QueryParam("tagId") @DefaultValue("0") int tagId,
			String xml)
	{
		Response retval = null;
		String json = null;
		try {
			for (Location location : LocationFactory.make(xml)) {
				json = new ObjectMapper().writeValueAsString(
					location);
				retval = handlePost(Location.class, request,
					json, tagId, getValidators(tagId));
			}
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn("{}",""+e);
		}
		return(retval);
	}

	/**
	 * Read a specific location.
	 */
	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleGet(Location.class, request, id));
	}

	/**
	 * Read a specific location as KML.
	 */
	@GET
	@Path("/{id}.kml")
	@Produces("application/vnd.google-earth.kml+xml")
	public Response handleExtGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleMimeGet(request, id));
	}

	/**
	 * Read a specific location as KML.
	 */
	@GET
	@Path("/{id}")
	@Produces("application/vnd.google-earth.kml+xml")
	public Response handleMimeGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		Response retval = null;
		try {
			Util.enforceBasicAuth(request);
			String kml = LocationFactory.makeKML(
				Datastore.DS().getLocation(id));
			retval = Response.status(200).entity(kml).build();
		}
		catch (APIException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn("{}",""+e);
		}
		return(retval);
	}

	@PUT
	@Path(value="/{id}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response handlePut(@Context HttpServletRequest request,
			String json, @PathParam(value="id") int id,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		return(handlePut(Location.class, request, json, id, tagId));
	}

	@DELETE
	@Path(value="/{id}")
	@Produces("application/json")
	public Response handleDelete(@Context HttpServletRequest request,
			@PathParam(value="id") int id,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		return(handleDelete(Location.class, request, id, tagId));
	}

	/**
	 * List all locations.
	 */
	@GET
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@QueryParam("page") @DefaultValue("0") int page,
			@QueryParam("pageSize") @DefaultValue("-1") int pgSize,
			@QueryParam("count") @DefaultValue("false") boolean cnt,
			@QueryParam("sort") @DefaultValue("name") String sort,
			@QueryParam(value="tagId") @DefaultValue("-1")
			int tagId, @QueryParam(value="tagName") String name)
	{
		Pagination pg = new Pagination(page, pgSize, cnt);
		pg.setSortBy(sort);
		return(handleGet(Location.class, request, pg, tagId, name));
	}

	/**
	 * Get all locations as KML.
	 */
	@GET
	@Path(value="/locations.kml")
	@Produces("application/vnd.google-earth.kml+xml")
	public Response handleManyExtGet(@Context HttpServletRequest request,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		return(handleManyMimeGet(request, tagId));
	}

	/**
	 * Get all locations as KML.
	 */
	@GET
	@Produces("application/vnd.google-earth.kml+xml")
	public Response handleManyMimeGet(@Context HttpServletRequest request,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		Response retval = null;
		try {
			Datastore dstore = Datastore.DS();
			Util.enforceBasicAuth(request);
			Credentials creds = Util.getCredentials(request);
			Pagination pg = new Pagination(1,MAX_KML_ELMNTS,false);
			Tag tag = dstore.getTag(tagId);
			int len = Datastore.MAX_LIST_SIZE;
			String kml = LocationFactory.makeKML(
				dstore.getLocations(tag, pg, creds));
			retval = Response.status(200).entity(""+kml).build();
		}
		catch (APIException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn("{}",""+e);
		}
		return(retval);
	}

	protected static Validator[] getValidators(long tagId)
	{
		Validator[] retval = new Validator[] {
			new CompleteAPIEntity("name", "latitude", "longitude"),
			new UniqueLocation(new Add2TagDuplicateHandler(tagId))
		};
		return(retval);
	}
}
