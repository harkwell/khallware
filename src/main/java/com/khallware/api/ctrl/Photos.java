// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.ctrl;

import com.khallware.api.dstore.Pagination;
import com.khallware.api.DatastoreException;
import com.khallware.api.APIException;
import com.khallware.api.Unauthorized;
import com.khallware.api.Datastore;
import com.khallware.api.domain.Tag;
import com.khallware.api.domain.Photo;
import com.khallware.api.domain.APIEntity;
import com.khallware.api.domain.Credentials;
import com.khallware.api.validation.Add2TagDuplicateHandler;
import com.khallware.api.validation.CompleteAPIEntity;
import com.khallware.api.validation.UniquePhoto;
import com.khallware.api.validation.Validator;
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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Path("/photos")
public class Photos extends CrudController<Photo>
{
	private static final Logger logger = LoggerFactory.getLogger(
		Photos.class);
	public static final String PROP_REPODIR = "images";
	public static final String PROP_REPOTHUMBS = "thumbs";
	public static final String DEF_REPO_THUMBS = "/tmp/thumbs";
	public static final String DEF_REPO_DIR = "/tmp/images";

	/**
	 * Create a new photo.
	 */
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response handlePost(@Context HttpServletRequest request,
			@QueryParam("tagId") @DefaultValue("0") int tagId,
			String json)
	{
		return(handlePost(Photo.class, request, json, tagId,
			new CompleteAPIEntity("name","path","description"),
			new UniquePhoto(new Add2TagDuplicateHandler(tagId))));
	}

	/**
	 * Read a specific photo.
	 */
	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleGet(Photo.class, request, id));
	}

	/**
	 * Read a specific photo image.
	 */
	@GET
	@Path("/{id}")
	@Produces("image/jpeg")
	public Response handleJpegGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id,
			@QueryParam("thumb") @DefaultValue("") String asThumb)
	{
		return(handleMimeGet(request, id, asThumb.isEmpty()));
	}

	/**
	 * Read a specific photo image.
	 */
	@GET
	@Path("/{id}.jpg")
	@Produces("image/jpeg")
	public Response handleAbsJpegGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id,
			@QueryParam("thumb") @DefaultValue("") String asThumb)
	{
		return(handleMimeGet(request, id, asThumb.isEmpty()));
	}

	/**
	 * Read a specific photo image.
	 */
	public Response handleMimeGet(HttpServletRequest request, int id,
			boolean asThumb)
	{
		Response retval = null;
		try {
			Util.enforceSecurity(request);
			Photo photo = Datastore.DS().getPhoto(id);
			File file = Util.resolveFile(photo.getPath(),
				PROP_REPODIR, DEF_REPO_DIR);
			retval = Response.status(200)
				.entity((asThumb)
					? Util.fileContentAsBytes(file)
					: Util.makeThumbnail(file, id,
						PROP_REPOTHUMBS,
						DEF_REPO_THUMBS))
				.header("Cache-Control",
					"no-transform, private, max-age="
					+Security.TIMEOUTMS)
				.build();
		}
		catch (APIException|IOException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
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
		return(handlePut(Photo.class, request, json, id, tagId));
	}

	@DELETE
	@Path(value="/{id}")
	@Produces("application/json")
	public Response handleDelete(@Context HttpServletRequest request,
			@PathParam(value="id") int id,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		return(handleDelete(Photo.class, request, id, tagId,
			photo -> delete((Photo)photo)));
	}

	/**
	 * List photos.
	 */
	@GET
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@QueryParam("page") @DefaultValue("0") int page,
			@QueryParam("pageSize") @DefaultValue("-1") int pgSize,
			@QueryParam("count") @DefaultValue("true") boolean cnt,
			@QueryParam("sort") @DefaultValue("modified") String s,
			@QueryParam("tagId") int tagId,
			@QueryParam(value="tagName") String name)
	{
		Pagination pg = new Pagination(page, pgSize, cnt);
		pg.setSortBy(s);
		return(handleGet(Photo.class, request, pg, tagId, name));
	}

	protected static void delete(Photo photo)
	{
		try {
			String filespec = photo.getPath();
			Files.delete(new File(filespec).toPath());
		}
		catch (IOException e) {
			logger.warn(""+e, e);
		}
	}
}
