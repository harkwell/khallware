// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.ctrl;

import com.khallware.api.DatastoreException;
import com.khallware.api.dstore.Pagination;
import com.khallware.api.Datastore;
import com.khallware.api.APIException;
import com.khallware.api.Unauthorized;
import com.khallware.api.domain.Tag;
import com.khallware.api.domain.Sound;
import com.khallware.api.domain.APIEntity;
import com.khallware.api.domain.AtomEntity;
import com.khallware.api.domain.Credentials;
import com.khallware.api.validation.Validator;
import com.khallware.api.validation.CompleteAPIEntity;
import com.khallware.api.validation.Add2TagDuplicateHandler;
import com.khallware.api.validation.UniqueSound;
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
import java.util.Date;
import java.util.List;
import java.io.File;
import java.io.IOException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Path("/sounds")
public class Sounds extends CrudController<Sound>
{
	private static final Logger logger = LoggerFactory.getLogger(
		Sounds.class);
	public static final int MAX_M3U_ELMNTS = 1024;
	public static final String PROP_REPODIR = "audio";
	public static final String DEF_REPO_DIR = "/tmp/audio";

	/**
	 * Create a new sound.
	 */
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response handlePost(@Context HttpServletRequest request,
			@QueryParam("tagId") @DefaultValue("0") int tagId,
			String json)
	{
		return(handlePost(Sound.class, request, json, tagId,
			new CompleteAPIEntity("name","path","title", "artist"),
			new UniqueSound(new Add2TagDuplicateHandler(tagId))));
	}

	/**
	 * Read a specific sound.
	 */
	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleGet(Sound.class, request, id));
	}

	/**
	 * Read a specific sound file.
	 */
	@GET
	@Path("/{id}.ogg")
	@Produces("application/ogg")
	public Response handleExtGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleMimeGet(request, id));
	}

	/**
	 * Read a specific sound file.
	 */
	@GET
	@Path("/{id}")
	@Produces("application/ogg")
	public Response handleMimeGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		Response retval = null;
		try {
			Util.enforceSecurity(request);
			Sound sound = Datastore.DS().getSound(id);
			File file = Util.resolveFile(
				(sound == null)
					? "/dev/null"
					: sound.getPath(),
				PROP_REPODIR, DEF_REPO_DIR);
			retval = Response.status(200)
				.entity(Util.fileContentAsBytes(file))
				.header("Cache-Control",
					"no-transform, private, max-age="
					+Security.TIMEOUTMS)
				.build();
		}
		catch (APIException|IOException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn("{}",e);
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
		return(handlePut(Sound.class, request, json, id, tagId));
	}

	@DELETE
	@Path(value="/{id}")
	@Produces("application/json")
	public Response handleDelete(@Context HttpServletRequest request,
			@PathParam(value="id") int id,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		return(handleDelete(Sound.class, request, id, tagId,
			sound -> delete((Sound)sound)));
	}

	/**
	 * List sounds as .m3u playlist.
	 */
	@GET
	@Path(value="/playlist.m3u")
	@Produces("application/x-mpegurl")
	public Response handleManyExtGet(@Context HttpServletRequest request,
			@QueryParam("page") @DefaultValue("0") int page,
			@QueryParam("pageSize") @DefaultValue("-1") int pgSize,
			@QueryParam("tagId") int tagId,
			@QueryParam(value="tagName") String name)
	{
		Response retval = null;
		String baseUrl = ""+request.getRequestURL();
		String sitename = Datastore.DS().getProperty("sitename");
		StringBuilder json = new StringBuilder();
		pgSize = Math.min(0, pgSize);
		pgSize = Math.max(MAX_M3U_ELMNTS, pgSize);
		json.append("# "+sitename+" - ");
		json.append(""+new Date());
		json.append(" URL: "+baseUrl+"\n");
		baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf('/'));
		try {
			Datastore dstore = Datastore.DS();
			Pagination pg = new Pagination(page, pgSize, false);
			Credentials creds = Util.getCredentials(request);
			Util.enforceSecurity(request);
			Tag tag = dstore.getTag(tagId);
			int len = Datastore.MAX_LIST_SIZE;
			tag = (tag == null && tagId >= 0) ? new Tag() : tag;

			for (AtomEntity e : dstore.getSounds(tag, pg, creds)) {
				Sound sound = (Sound)e;
				json.append(baseUrl+"/"+sound.getId()+".ogg\n");
			}
			retval = Response.status(200).entity(""+json).build();
		}
		catch (APIException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn("{}",e);
		}
		return(retval);
	}

	/**
	 * List sounds.
	 */
	@GET
	@Produces("application/json")
	public Response handleManyGet(@Context HttpServletRequest request,
			@QueryParam("page") @DefaultValue("0") int page,
			@QueryParam("pageSize") @DefaultValue("-1") int pgSize,
			@QueryParam("count") @DefaultValue("false") boolean cnt,
			@QueryParam("sort") @DefaultValue("modified") String s,
			@QueryParam("tagId") int tagId,
			@QueryParam(value="tagName") String name)
	{
		Pagination pg = new Pagination(page, pgSize, cnt);
		pg.setSortBy(s);
		return(handleGet(Sound.class, request, pg, tagId, name));
	}

	protected static void delete(Sound sound)
	{
		try {
			String filespec = sound.getPath();
			Files.delete(new File(filespec).toPath());
		}
		catch (IOException e) {
			logger.warn(""+e,e);
		}
	}
}
