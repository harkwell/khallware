// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.ctrl;

import com.khallware.api.dstore.Pagination;
import com.khallware.api.DatastoreException;
import com.khallware.api.Unauthorized;
import com.khallware.api.APIException;
import com.khallware.api.Datastore;
import com.khallware.api.domain.Tag;
import com.khallware.api.domain.Blog;
import com.khallware.api.domain.Comment;
import com.khallware.api.domain.Credentials;
import com.khallware.api.validation.Validator;
import com.khallware.api.validation.UniqueComment;
import com.khallware.api.validation.CompleteAPIEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Path("/blogs/{blogId}/comments")
public class Comments extends CrudController<Comment>
{
	private static final Logger logger = LoggerFactory.getLogger(
		Comments.class);

	/**
	 * Create a new comment.
	 */
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response handlePost(@Context HttpServletRequest request,
			String json)
	{
		return(handlePost(Comment.class, request, json, Tag.ROOT,
			new CompleteAPIEntity("content","description"),
			new UniqueComment()));
	}

	/**
	 * Read a specific comment.
	 */
	@GET
	@Path("{commentId}")
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@PathParam("commentId") int commentId)
	{
		return(handleGet(Comment.class, request, commentId));
	}

	@PUT
	@Path(value="{commentId}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response handlePut(@Context HttpServletRequest request,
			String json, @PathParam("commentId") int id,
			@QueryParam("tagId") @DefaultValue("0") int ignore)
	{
		return(handlePut(Comment.class, request, json, id, ignore));
	}

	@DELETE
	@Path(value="{commentId}")
	@Produces("application/json")
	public Response handleDelete(@Context HttpServletRequest request,
			@PathParam("commentId") int id,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		return(handleDelete(Comment.class, request, id, tagId));
	}

	@DELETE
	@Produces("application/json")
	public Response handleDeleteAll(@Context HttpServletRequest request,
			@PathParam("blogId") int blogId)
	{
		Response retval = null;
		try {
			Blog blog = Datastore.DS().getBlog(blogId);
			String s = Comments.class.getSimpleName().toLowerCase();
			List<Comment> list = Datastore.DS().getComments(blog,
				new Pagination(), Util.getCredentials(request));
			String json = CrudController.toJson(s, list);

			for (Comment comment : list) {
				Datastore.DS().deleteComment(comment.getId());
			}
			retval = Response.status(200).entity(json).build();
		}
		catch (APIException|IOException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn("{}",""+e);
		}
		return(retval);
	}

	/**
	 * List comments.
	 */
	@GET
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@QueryParam("page") @DefaultValue("0") int page,
			@QueryParam("pageSize") @DefaultValue("-1") int pgSize,
			@QueryParam("count") @DefaultValue("false") boolean cnt,
			@QueryParam("sort") @DefaultValue("modified") String st,
			@PathParam("blogId") @DefaultValue("-1") int blogId)
	{
		Response retval = null;
		try {
			Util.enforceSecurity(request);
			String s = Comments.class.getSimpleName().toLowerCase();
			String json = null;
			Blog blog = Datastore.DS().getBlog(blogId);
			Pagination pgInfo = new Pagination(page, pgSize, cnt);
			pgInfo.setSortBy(st);

			if (blog == null) {
				throw new DatastoreException("no such blog: "
					+blogId);
			}
			List<Comment> list = Datastore.DS().getComments(blog,
				pgInfo, Util.getCredentials(request));
			json = new ObjectMapper().writeValueAsString(list);
			json = "{\""+s+"\":"+json+"}";
			retval = Response.status(200).entity(json).build();
		}
		catch (APIException|IOException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn("{}",""+e);
		}
		return(retval);
	}
}
