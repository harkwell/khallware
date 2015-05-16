// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.ctrl;

import com.khallware.api.APIException;
import com.khallware.api.DatastoreException;
import com.khallware.api.dstore.Pagination;
import com.khallware.api.Unauthorized;
import com.khallware.api.Datastore;
import com.khallware.api.domain.Tag;
import com.khallware.api.domain.FileItem;
import com.khallware.api.domain.APIEntity;
import com.khallware.api.domain.Credentials;
import com.khallware.api.validation.Validator;
import com.khallware.api.validation.CompleteAPIEntity;
import com.khallware.api.validation.Add2TagDuplicateHandler;
import com.khallware.api.validation.UniqueFileItem;
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

@Path("/fileitems")
public class FileItems extends CrudController<FileItem>
{
	private static final Logger logger = LoggerFactory.getLogger(
		FileItems.class);
	public static final String PROP_REPODIR = "fileitem";
	public static final String DEF_REPO_DIR = "/tmp/fileitems";

	/**
	 * Create a new fileitem.
	 */
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response handlePost(@Context HttpServletRequest request,
			@QueryParam("tagId") @DefaultValue("0") int tagId,
			String json)
	{
		return(handlePost(FileItem.class, request, json, tagId,
			new CompleteAPIEntity("name","path","ext"),
			new UniqueFileItem(new Add2TagDuplicateHandler(tagId))
		));
	}

	/**
	 * Read a specific fileitem.
	 */
	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleGet(FileItem.class, request, id));
	}

	/**
	 * Read a specific fileitem.
	 */
	@GET
	@Path("/{id}")
	@Produces("application/octet-stream")
	public Response handleVagueGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleMimeGet(request, id));
	}

	/**
	 * Read a specific pdf.
	 */
	@GET
	@Path("/{id}.pdf")
	@Produces("application/pdf")
	public Response handlePdfGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleMimeGet(request, id));
	}

	/**
	 * Read a specific odt.
	 */
	@GET
	@Path("/{id}.odt")
	@Produces("application/vnd.oasis.opendocument.text")
	public Response handleOdtGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleMimeGet(request, id));
	}

	/**
	 * Read a specific fileitem.
	 */
	public Response handleMimeGet(HttpServletRequest request, int id)
	{
		Response retval = null;
		try {
			Util.enforceSecurity(request);
			FileItem fileitem = Datastore.DS().getFileItem(id);
			File file = Util.resolveFile(fileitem.getPath(),
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
		return(handlePut(FileItem.class, request, json, id, tagId));
	}

	@DELETE
	@Path(value="/{id}")
	@Produces("application/json")
	public Response handleDelete(@Context HttpServletRequest request,
			@PathParam(value="id") int id,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		return(handleDelete(FileItem.class, request, id, tagId,
			fileitem -> delete((FileItem)fileitem)));
	}

	/**
	 * List fileitems.
	 */
	@GET
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@QueryParam("page") @DefaultValue("0") int page,
			@QueryParam("pageSize") @DefaultValue("-1") int pgSize,
			@QueryParam("count") @DefaultValue("true") boolean cnt,
			@QueryParam("sort") @DefaultValue("name") String sort,
			@QueryParam("tagId") int tagId,
			@QueryParam(value="tagName") String name)
	{
		Pagination pg = new Pagination(page, pgSize, cnt);
		pg.setSortBy(sort);
		return(handleGet(FileItem.class, request, pg, tagId, name));
	}

	protected static void delete(FileItem fileItem)
	{
		try {
			String filespec = fileItem.getPath();
			Files.delete(new File(filespec).toPath());
		}
		catch (IOException e) {
			logger.warn(""+e, e);
		}
	}
}
