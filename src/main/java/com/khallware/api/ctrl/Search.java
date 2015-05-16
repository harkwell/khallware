// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.ctrl;

import com.khallware.api.dstore.Pagination;
import com.khallware.api.DatastoreException;
import com.khallware.api.Datastore;
import com.khallware.api.domain.Tag;
import com.khallware.api.domain.Blog;
import com.khallware.api.domain.Photo;
import com.khallware.api.domain.Video;
import com.khallware.api.domain.Event;
import com.khallware.api.domain.Sound;
import com.khallware.api.domain.Location;
import com.khallware.api.domain.Bookmark;
import com.khallware.api.domain.APIEntity;
import com.khallware.api.domain.Credentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Path("/search")
public class Search
{
	private static final Logger logger = LoggerFactory.getLogger(
		Search.class);

	/**
	 * Invokes a new search.
	 */
	@POST
	@Produces("application/json")
	public Response handlePost(@Context HttpServletRequest request,
			@QueryParam("tagId") @DefaultValue("-1") int tagId,
			@QueryParam("page") @DefaultValue("0") int page,
			@QueryParam("pageSize") @DefaultValue("25") int pgSize,
			@QueryParam("count") @DefaultValue("true") boolean cnt,
			@QueryParam("sort") @DefaultValue("class") String sort,
			@QueryParam("type") String type,
			String pattern)
	{
		Response retval = null;
		StringBuilder json = new StringBuilder();
		Class[] domain = new Class[] { // order matters!
			Photo.class, Video.class, Blog.class, Event.class,
			Location.class, Sound.class, Bookmark.class
		};
		try {
			Util.enforceSecurity(request);
			ObjectMapper mapper = new ObjectMapper();
			Credentials creds = Util.getCredentials(request);
			Pagination pg = new Pagination(page, pgSize, cnt);
			Tag tag = (tagId >= 0)
				? Datastore.DS().getTag(tagId)
				: null;
			List<APIEntity> list = new ArrayList<>();
			List<APIEntity> rslt = Datastore.DS().search(
				pg.setSortBy(sort),
				Util.getCredentials(request), tag, type,
				pattern);
			String cname = null;
			logger.info("({}) "+this.getClass().getSimpleName()
				+" POST \"{}\" {} results",
				creds.getUsername(), pattern, pg.getCount());
			// json.append("{\"count\":"+pg.getCount());
			json.append("{\"count\":-1");

			for (Class clazz : domain) {
				cname = clazz.getSimpleName().toLowerCase();
				cname += "s";
				list.clear();

				for (APIEntity entity : rslt) {
					if (clazz.isAssignableFrom(
							entity.getClass())) {
						list.add(entity);
					}
				}
				json.append(",\""+cname+"\":");
				json.append(mapper.writeValueAsString(list));
			}
			json.append("}");
			retval = Response.status(200).entity(""+json).build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}
}
