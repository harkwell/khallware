// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.ctrl;

import com.khallware.api.dstore.Pagination;
import com.khallware.api.Datastore;
import com.khallware.api.EventFactory;
import com.khallware.api.APIException;
import com.khallware.api.Unauthorized;
import com.khallware.api.DatastoreException;
import com.khallware.api.validation.Validator;
import com.khallware.api.validation.CompleteEvent;
import com.khallware.api.validation.Add2TagDuplicateHandler;
import com.khallware.api.validation.UniqueEvent;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.Event;
import com.khallware.api.domain.Group;
import com.khallware.api.domain.Tag;
import biweekly.component.VEvent;
import biweekly.Biweekly;
import biweekly.ICalendar;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
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
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/events")
public class Events extends CrudController<Event>
{
	public static final long MAX_ICS_ELMNTS = 1024;
	private static final Logger logger = LoggerFactory.getLogger(
		Events.class);

	/**
	 * Create a new event.
	 */
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response handlePost(@Context HttpServletRequest request,
			@QueryParam("tagId") @DefaultValue("0") int tagId,
			String json)
	{
		return(handlePost(Event.class, request, json, tagId,
			new CompleteEvent(),
			new UniqueEvent(new Add2TagDuplicateHandler(tagId))));
	}

	/**
	 * Create a new event from a text/calendar post.
	 */
	@POST
	@Consumes("text/calendar")
	@Produces("text/calendar")
	public Response handleMimePost(@Context HttpServletRequest request,
			@QueryParam("tagId") @DefaultValue("0") int tagId,
			String text)
	{
		Response retval = null;
		logger.trace("text/calendar = ("+text+")");
		try {
			Util.enforceSecurity(request);
			Credentials creds = Datastore.DS().getCredentials(
				Util.getCredentials(request));
			int user = creds.getId();
			Group group = creds.getGroup();

			for (Event event : EventFactory.make(text)) {
				event.setUser(user);
				event.setGroup(group);
				logger.info("given event ("+event+")");
				Datastore.DS().save(event);

				if (tagId > Tag.ROOT) {
					Datastore.DS().add2Tag(event, tagId);
				}
			}
			retval = Response.status(200).entity(text).build();
		}
		catch (APIException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	/**
	 * Read a specific event.
	 */
	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(super.handleGet(Event.class, request, id));
	}

	/**
	 * Read a specific event as text/calendar.
	 */
	@GET
	@Path("/{id}.ics")
	@Produces("text/calendar")
	public Response handleExtGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleMimeGet(request, id));
	}

	/**
	 * Read a specific event as text/calendar.
	 */
	@GET
	@Path("/{id}")
	@Produces("text/calendar")
	public Response handleMimeGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		Response retval = null;
		try {
			Util.enforceSecurity(request);
			String text = toICS(Datastore.DS().getEvent(id));
			retval = Response.status(200).entity(text).build();
		}
		catch (APIException|DatastoreException e) {
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
		return(handlePut(Event.class, request, json, id, tagId));
	}

	@DELETE
	@Path(value="/{id}")
	@Produces("application/json")
	public Response handleDelete(@Context HttpServletRequest request,
			@PathParam(value="id") int id,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		return(handleDelete(Event.class, request, id, tagId));
	}

	/**
	 * List all events.
	 */
	@GET
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@QueryParam("page") @DefaultValue("0") int page,
			@QueryParam("pageSize") @DefaultValue("-1") int pgSize,
			@QueryParam("sort") @DefaultValue("name") String sort,
			@QueryParam("tagId") int tagId,
			@QueryParam(value="tagName") String name)
	{
		Pagination pg = new Pagination(page, pgSize, false);
		pg.setSortBy(sort);
		return(handleGet(Event.class, request, pg, tagId, name));
	}

	/**
	 * List all events as one large ics file.
	 */
	@GET
	@Path(value="/calendar.ics")
	@Produces("text/calendar")
	public Response handleManyExtGet(@Context HttpServletRequest request,
			@QueryParam("sort") @DefaultValue("name") String sort,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		return(handleManyMimeGet(request, sort, tagId));
	}

	/**
	 * List all events as one large ics file.
	 */
	@GET
	@Produces("text/calendar")
	public Response handleManyMimeGet(@Context HttpServletRequest request,
			@QueryParam("sort") @DefaultValue("name") String sort,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		Response retval = null;
		try {
			Datastore dstore = Datastore.DS();
			Credentials creds = Util.getCredentials(request);
			Pagination pg = new Pagination(1,MAX_ICS_ELMNTS,false);
			Util.enforceSecurity(request);
			Tag tag = dstore.getTag(tagId);
			int len = Datastore.MAX_LIST_SIZE;
			String text = toICS(dstore.getEvents(
				tag, pg.setSortBy(sort), creds));
			retval = Response.status(200).entity(text).build();
		}
		catch (APIException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	public String toICS(Event... events)
	{
		return(toICS(Arrays.asList(events)));
	}

	public String toICS(List<Event> events)
	{
		String retval = "";
		ICalendar ical = new ICalendar();
		ICalendar tmp = null;
		VEvent vEvent = null;

		for (Event event : events) {
			if ((tmp = Biweekly.parse(event.getIcs()).first())
					== null) {
				continue;
			}
			if (tmp.getEvents().isEmpty()) {
				continue;
			}
			if ((vEvent = tmp.getEvents().get(0)) == null) {
				continue;
			}
			ical.addEvent(vEvent);
		}
		retval = Biweekly.write(ical).go();
		return(retval);
	}
}
