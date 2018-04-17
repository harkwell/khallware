// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.ctrl;

import com.khallware.api.Datastore;
import com.khallware.api.Unauthorized;
import com.khallware.api.APIException;
import com.khallware.api.ServletContainer;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import java.net.URL;
import java.io.IOException;
import java.io.FileInputStream;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Path("/static")
public class StaticContent
{
	public static final String PROP_CAPTCHA_FILE =
		ServletContainer.PROP_CAPTCHA_FILE;
	private static final Logger logger = LoggerFactory.getLogger(
		StaticContent.class);

	/**
	 * Read specific static content (URL).
	 */
	@GET
	@Path("/{file}")
	@Produces("text/html")
	public Response handleGetHtml(@Context HttpServletRequest request,
			@PathParam(value="file") String filename)
	{
		return(handleGet(request, "/WEB-INF/static/"+filename));
	}

	@GET
	@Path("admin/{file}")
	@Produces("text/html")
	public Response handleGetAdminOnly(@Context HttpServletRequest request,
			@PathParam(value="file") String filename)
	{
		Response retval = null;
		try {
			Admin.enforceThatUserIsAdmin(request);
			retval = handleGet(request,
				"/WEB-INF/static/admin/"+filename);
		}
		catch (Unauthorized e) {
			retval = Util.failRequest(e);
			logger.warn(""+e);
		}
		return(retval);
	}

	@GET
	@Path("scripts/{file}")
	@Produces("text/html")
	public Response handleGetScripts(@Context HttpServletRequest request,
			@PathParam(value="file") String filename)
	{
		return(handleGet(request, "/WEB-INF/static/scripts/"+filename));
	}

	@GET
	@Path("templates/{file}")
	@Produces("text/html")
	public Response handleGetTemplates(@Context HttpServletRequest request,
			@PathParam(value="file") String fname)
	{
		return(handleGet(request, "/WEB-INF/static/templates/"+fname));
	}

	@GET
	@Path("captcha/image.png")
	@Produces("image/png")
	public Response handleGetCaptcha(@Context HttpServletRequest request)
	{
		Response retval = null;
		String fname = Datastore.DS().getProperty(PROP_CAPTCHA_FILE);
		try {
			logger.info("requesting captcha file: {}", fname);
			retval = Response.status(200)
				.entity(Util.fileContentAsBytes(
					new FileInputStream(fname)))
				.header("Cache-Control",
					"no-transform, private, max-age=0")
				.build();
		}
		catch (IOException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	protected Response handleGet(HttpServletRequest request, String src)
	{
		Response retval = null;
		URL url = null;
		try {
			Util.enforceSecurity(request);
			url = request.getServletContext().getResource(src);
			logger.info("requesting file \"{}\"", ""+url);
			retval = Response.status(200)
				.entity(Util.fileContentAsBytes(
					url.openStream()))
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
}
