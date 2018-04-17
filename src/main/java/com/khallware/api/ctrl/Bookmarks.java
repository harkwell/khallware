// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.ctrl;

import com.khallware.api.Datastore;
import com.khallware.api.Unauthorized;
import com.khallware.api.DatastoreException;
import com.khallware.api.dstore.Pagination;
import com.khallware.api.validation.Add2TagDuplicateHandler;
import com.khallware.api.validation.CompleteAPIEntity;
import com.khallware.api.validation.UniqueBookmark;
import com.khallware.api.validation.Validator;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.Bookmark;
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
import java.util.List;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Path("/bookmarks")
public class Bookmarks extends CrudController<Bookmark>
{
	private static final Logger logger = LoggerFactory.getLogger(
		Bookmarks.class);

	public Bookmark read(int id, Credentials creds)
			throws DatastoreException, Unauthorized
	{
		return((Bookmark)
			Util.enforceRead(Datastore.DS().getBookmark(id),creds));
	}

	public List<Bookmark> read(Pagination pg, Credentials creds)
			throws DatastoreException
	{
		return(Datastore.DS().getBookmarks(pg, creds));
	}

	public List<Bookmark> read(Tag tag, Pagination pg, Credentials creds)
			throws DatastoreException
	{
		return(Datastore.DS().getBookmarks(tag, pg, creds));
	}

	/**
	 * Create a new bookmark.
	 */
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response handlePost(@Context HttpServletRequest request,
			@QueryParam("tagId") @DefaultValue("0") int tagId,
			String json)
	{
		return(handlePost(Bookmark.class, request, json, tagId,
			new CompleteAPIEntity("name","url"),
			new UniqueBookmark(new Add2TagDuplicateHandler(tagId))
		));
	}

	/**
	 * Read a specific bookmark (URL).
	 */
	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleGet(Bookmark.class, request, id));
	}

	@PUT
	@Path(value="/{id}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response handlePut(@Context HttpServletRequest request,
			String json, @PathParam(value="id") int id,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		return(handlePut(Bookmark.class, request, json, id, tagId));
	}

	@DELETE
	@Path(value="/{id}")
	@Produces("application/json")
	public Response handleDelete(@Context HttpServletRequest request,
			@PathParam(value="id") int id,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		return(handleDelete(Bookmark.class, request, id, tagId));
	}

	/**
	 * List bookmarks (URLs).
	 */
	@GET
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@QueryParam("tagId") int tagId,
			@QueryParam("sort") @DefaultValue("modified") String s,
			@QueryParam("pageSize") @DefaultValue("25") int pgSize,
			@QueryParam("count") @DefaultValue("false") boolean cnt,
			@QueryParam("page") @DefaultValue("0") int page,
			@QueryParam(value="tagName") String name)
	{
		Pagination pg = new Pagination(page, pgSize, cnt);
		pg.setSortBy(s);
		return(handleGet(Bookmark.class, request, pg, tagId, name));
	}
}
