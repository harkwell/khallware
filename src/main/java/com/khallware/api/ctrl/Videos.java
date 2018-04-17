// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.ctrl;

import com.khallware.api.dstore.Pagination;
import com.khallware.api.DatastoreException;
import com.khallware.api.Datastore;
import com.khallware.api.APIException;
import com.khallware.api.Unauthorized;
import com.khallware.api.domain.Tag;
import com.khallware.api.domain.Video;
import com.khallware.api.domain.APIEntity;
import com.khallware.api.domain.Credentials;
import com.khallware.api.validation.Validator;
import com.khallware.api.validation.CompleteAPIEntity;
import com.khallware.api.validation.Add2TagDuplicateHandler;
import com.khallware.api.validation.UniqueVideo;
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

@Path("/videos")
public class Videos extends CrudController<Video>
{
	private static final Logger logger = LoggerFactory.getLogger(
		Videos.class);
	public static final String PROP_REPODIR = "images";
	public static final String DEF_REPO_DIR = "/tmp/images";
	public static final File repoDir = new File(
		System.getProperty(PROP_REPODIR, DEF_REPO_DIR));

	/**
	 * Create a new video.
	 */
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response handlePost(@Context HttpServletRequest request,
			@QueryParam("tagId") @DefaultValue("0") int tagId,
			String json)
	{
		return(handlePost(Video.class, request, json, tagId,
			new CompleteAPIEntity("name","path","description"),
			new UniqueVideo(new Add2TagDuplicateHandler(tagId))));
	}

	/**
	 * Read a specific video.
	 */
	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleGet(Video.class, request, id));
	}

	/**
	 * Read a specific flv video via file extension.
	 */
	@GET
	@Path("/{id}.flv")
	@Produces("video/x-flv")
	public Response handleFlvGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleBinaryContentGet(request, replaceFileExtension(
			getVideoPath(id), "flv")));
	}

	/**
	 * Read a specific video image via file extension.
	 */
	@GET
	@Path("/{id}.jpg")
	@Produces("image/jpeg")
	public Response handleJpgGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleBinaryContentGet(request, replaceFileExtension(
			getVideoPath(id), "jpg")));
	}

	/**
	 * Read a specific video image via file extension.
	 */
	@GET
	@Path("/{id}.mp4")
	@Produces("video/mp4")
	public Response handleMpgGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleMimeGet(request, id));
	}

	/**
	 * Read a specific video image.
	 */
	@GET
	@Path("/{id}")
	@Produces("video/mp4")
	public Response handleMimeGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleBinaryContentGet(request, getVideoPath(id)));
	}

	@PUT
	@Path(value="/{id}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response handlePut(@Context HttpServletRequest request,
			String json, @PathParam(value="id") int id,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		return(handlePut(Video.class, request, json, id, tagId));
	}

	@DELETE
	@Path(value="/{id}")
	@Produces("application/json")
	public Response handleDelete(@Context HttpServletRequest request,
			@PathParam(value="id") int id,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		return(handleDelete(Video.class, request, id, tagId,
			video -> delete((Video)video)));
	}

	/**
	 * List videos.
	 */
	@GET
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@QueryParam("page") @DefaultValue("0") int page,
			@QueryParam("pageSize") @DefaultValue("-1") int pgSize,
			@QueryParam("count") @DefaultValue("false") boolean cnt,
			@QueryParam("sort") @DefaultValue("name") String sort,
			@QueryParam("tagId") int tagId,
			@QueryParam(value="tagName") String name)
	{
		Pagination pg = new Pagination(page, pgSize, cnt);
		pg.setSortBy(sort);
		return(handleGet(Video.class, request, pg, tagId, name));
	}

	public static String replaceFileExtension(String filespec, String ext)
	{
		String retval = null;
		try {
			int idx = filespec.lastIndexOf(".");
			retval = filespec.substring(0, idx) + ".";
			idx = ext.lastIndexOf(".");
			retval += ext.substring(Math.max(0, idx));
		}
		catch (Exception e) {
			logger.error(""+e, e);
		}
		return(retval);
	}

	protected String getVideoPath(int id)
	{
		String retval = null;
		try {
			Video video = Datastore.DS().getVideo(id);
			retval = video.getPath();
		}
		catch (DatastoreException e) {
			logger.error(""+e, e);
		}
		return(retval);
	}

	protected Response handleBinaryContentGet(HttpServletRequest request,
			String filespec)
	{
		Response retval = null;
		try {
			Util.enforceSecurity(request);
			File file = Util.resolveFile(filespec, PROP_REPODIR,
				DEF_REPO_DIR);
			retval = Response.status(200)
				.entity(Util.fileContentAsBytes(file))
				.header("Cache-Control",
					"no-transform, private, max-age="
					+Security.TIMEOUTMS)
				.build();
		}
		catch (APIException|IOException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	protected static void delete(Video video)
	{
		try {
			String filespec = video.getPath();
			Files.delete(new File(filespec).toPath());
		}
		catch (IOException e) {
			logger.warn(""+e, e);
		}
	}
}
