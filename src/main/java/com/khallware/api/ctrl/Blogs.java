// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.ctrl;

import com.khallware.api.dstore.Pagination;
import com.khallware.api.Datastore;
import com.khallware.api.Unauthorized;
import com.khallware.api.DatastoreException;
import com.khallware.api.validation.CompleteAPIEntity;
import com.khallware.api.validation.UniqueBlog;
import com.khallware.api.validation.Validator;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.Blog;
import com.khallware.api.domain.Tag;
import javax.servlet.http.HttpServletRequest;
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
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/blogs")
public class Blogs extends CrudController<Blog>
{
	private static final Logger logger = LoggerFactory.getLogger(
		Blogs.class);

	/**
	 * Create a new blog.
	 */
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response handlePost(@Context HttpServletRequest request,
			@QueryParam("tagId") @DefaultValue("0") int tagId,
			String json)
	{
		return(handlePost(Blog.class, request, json, tagId,
			new CompleteAPIEntity("content"), new UniqueBlog()));
	}

	/**
	 * Read a specific blog.
	 */
	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleGet(Blog.class, request, id));
	}

	@PUT
	@Path(value="/{id}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response handlePut(@Context HttpServletRequest request,
			String json, @PathParam(value="id") int id,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		return(handlePut(Blog.class, request, json, id, tagId));
	}

	@DELETE
	@Path(value="/{id}")
	@Produces("application/json")
	public Response handleDelete(@Context HttpServletRequest request,
			@PathParam(value="id") int id,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		return(handleDelete(Blog.class, request, id, tagId));
	}

	/**
	 * List blogs.
	 */
	@GET
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@QueryParam("page") @DefaultValue("0") int page,
			@QueryParam("pageSize") @DefaultValue("-1") int pgSize,
			@QueryParam("sort") @DefaultValue("created") String val,
			@QueryParam("count") @DefaultValue("true") boolean cnt,
			@QueryParam("tagId") int tagId,
			@QueryParam(value="tagName") String name)
	{
		Pagination pg = new Pagination(page, pgSize, cnt);
		pg.setSortBy(val);
		return(handleGet(Blog.class, request, pg, tagId, name));
	}
}
