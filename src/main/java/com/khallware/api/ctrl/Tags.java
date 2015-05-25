// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.ctrl;

import com.khallware.api.dstore.Pagination;
import com.khallware.api.DatastoreException;
import com.khallware.api.Datastore;
import com.khallware.api.APIException;
import com.khallware.api.Unauthorized;
import com.khallware.api.domain.Tag;
import com.khallware.api.domain.APIEntity;
import com.khallware.api.domain.Credentials;
import com.khallware.api.validation.Validator;
import com.khallware.api.validation.CompleteAPIEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
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
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Path("/tags")
public class Tags extends CrudController<Tag>
{
	private static final Logger logger = LoggerFactory.getLogger(
		Tags.class);

	/**
	 * Create a new tag.
	 */
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response handlePost(@Context HttpServletRequest request,
			String json)
	{
		return(handlePost(Tag.class, request, json,
			new CompleteAPIEntity("name")));
	}

	/**
	 * Read a specific tag.
	 */
	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleGet(Tag.class, request, id));
	}

	@PUT
	@Path(value="/{id}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response handlePut(@Context HttpServletRequest request,
			String json, @PathParam(value="id") int id)
	{
		return(handlePut(Tag.class, request, json, id, Tag.ROOT));
	}

	@DELETE
	@Path(value="/{id}")
	@Produces("application/json")
	public Response handleDelete(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		Response retval = null;
		try {
			Datastore dstore = Datastore.DS();
			Util.enforceSecurity(request);
			Credentials creds = dstore.getCredentials(
				Util.getCredentials(request));
			Tag tag = dstore.getTag(id);
			Util.enforceRead(tag, creds);
			Util.enforceWrite(tag, creds);
			retval = handleGet(Tag.class, creds, id);
			Datastore.DS().deleteTag(id);
		}
		catch (IOException|APIException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	/**
	 * List tags.
	 */
	@GET
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@QueryParam("page") @DefaultValue("0") int page,
			@QueryParam("pageSize") @DefaultValue("-1") int pgSize,
			@QueryParam("tagName") @DefaultValue("") String tagName,
			@QueryParam("count") @DefaultValue("false") boolean cnt,
			@QueryParam("tagId") @DefaultValue("-1") int tagId,
			@QueryParam("parentId")@DefaultValue("-1") int parentId,
			@QueryParam(value="parentName") @DefaultValue("") 
			String name)
	{
		Response retval = null;
		List<Tag> tags = new ArrayList<>();
		try {
			Pagination pg = new Pagination(page, pgSize, cnt);
			Credentials creds = Util.getCredentials(request);
			Util.enforceSecurity(request);
			pg.setSortBy("name");

			if (tagId >= Tag.ROOT) {
				tags.add(Datastore.DS().getTag(tagId));
			}
			if (!tagName.isEmpty()) {
				tags.addAll(
					Datastore.DS().getTagsByName(
						pg, tagName, creds));
			}
			if (tags.size() == 0 || parentId >= Tag.ROOT) {
				Tag parent = Datastore.DS().getTag(parentId);
				parent = (parent == null) ? new Tag() : parent;
				tags.addAll(Datastore.DS().getTags(parent));
			}
			String json = 
				CrudController.toJson(this.getClass(), tags);
			retval = Response.status(200).entity(json).build();
		}
		catch (APIException|IOException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}
}
