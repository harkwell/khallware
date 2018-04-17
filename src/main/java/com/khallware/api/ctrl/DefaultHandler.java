// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.ctrl;

import com.khallware.api.APIException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.Path;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DefaultHandler
{
	private static final Logger logger = LoggerFactory.getLogger(
		DefaultHandler.class);

	@Path("/")
	public Response handleRequest(@Context HttpServletRequest request)
	{
		Response retval = null;
		try {
			Util.enforceSecurity(request);
			StringBuilder sb = new StringBuilder();
			logger.trace("handleRequest(): default handler found");
			sb.append("/bookmarks\n");
			sb.append("/contacts\n");
			sb.append("/events\n");
			sb.append("/photos\n");
			sb.append("/tags\n");
			retval = Response.status(404).entity(""+sb).build();
		}
		catch (APIException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}
}
