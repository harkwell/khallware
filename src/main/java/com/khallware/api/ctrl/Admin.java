// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.ctrl;

import com.khallware.api.validation.CompleteCredentials;
import com.khallware.api.validation.UniqueCredentials;
import com.khallware.api.validation.UniqueGroup;
import com.khallware.api.domain.Session;
import com.khallware.api.domain.Group;
import com.khallware.api.domain.Edge;
import com.khallware.api.domain.Credentials;
import com.khallware.api.dstore.Pagination;
import com.khallware.api.DatastoreException;
import com.khallware.api.Unauthorized;
import com.khallware.api.Datastore;
import com.khallware.api.APIException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Path("/admin")
public class Admin
{
	private static final Logger logger = LoggerFactory.getLogger(
		Admin.class);

	@POST
	@Path("/groups")
	@Consumes("application/json")
	@Produces("application/json")
	public Response groupPost(@Context HttpServletRequest request,
			String json)
	{
		Response retval = null;
		try {
			Admin.enforceThatUserIsAdmin(request);
			Group group = new ObjectMapper().readValue(json,
				Group.class);

			if (group.getId() == Group.ROOT) {
				group.setId(Group.UNKNOWN);
			}
			logger.trace("new group posted ({})", group);
			new UniqueGroup().enforce(group);
			Datastore.DS().save(group);
			retval = Response.status(200)
				.entity(new ObjectMapper().writeValueAsString(
					group))
				.build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	@POST
	@Path("/groups/{id}/children")
	@Consumes("application/json")
	@Produces("application/json")
	public Response edgePost(@Context HttpServletRequest request,
			@PathParam("id") int id, String json)
	{
		Response retval = null;
		try {
			Admin.enforceThatUserIsAdmin(request);
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> map = mapper.readValue(json,
				new TypeReference<HashMap<String,Object>>(){});
			Integer child = Integer.parseInt(""+map.get("child"));
			Group parent = Datastore.DS().getGroup(id);
			Edge edge = new Edge();
			edge.setGroup(Datastore.DS().getGroup(child));
			edge.setParent(parent);
			logger.trace("new edge posted \"{}\"", edge);
			Datastore.DS().save(edge);
			retval = Response.status(200)
				.entity(mapper.writeValueAsString(edge))
				.build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	@POST
	@Path("/users")
	@Consumes("application/json")
	@Produces("application/json")
	public Response credentialPost(@Context HttpServletRequest request,
			@QueryParam("group") @DefaultValue("-1") int groupId,
			String json)
	{
		Response retval = null;
		try {
			Admin.enforceThatUserIsAdmin(request);
			Credentials creds = new ObjectMapper().readValue(json,
				Credentials.class);
			Group group = Datastore.DS().getGroup(groupId);
			creds = new Credentials(creds.getUsername(),
				Util.hash(creds.getPassword()), group,
				creds.getEmail(), null, false);
			logger.trace("posted new user ({})", creds);
			new CompleteCredentials().enforce(creds);
			new UniqueCredentials().enforce(creds);
			Datastore.DS().save(creds);
			retval = Response.status(200)
				.entity(new ObjectMapper().writeValueAsString(
					creds))
				.build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	@GET
	@Path("/users")
	@Consumes("application/json")
	@Produces("application/json")
	public Response credentialsGet(@Context HttpServletRequest request,
			@QueryParam("pageSize") @DefaultValue("25") int pgSize,
			@QueryParam("page") @DefaultValue("0") int page)
	{
		Response retval = null;
		try {
			String json = null;
			Admin.enforceThatUserIsAdmin(request);
			Pagination pg = new Pagination(page, pgSize, false);
			List<Credentials> rslt =
				Datastore.DS().findCredentials(pg, null);

			for (Credentials c : rslt) {
				c.setQuota(Datastore.DS().getAvailableQuota(c));
			}
			json = CrudController.toJson("users", rslt);
			retval = Response.status(200).entity(json).build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	@GET
	@Path("/users/{id}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response credentialsGet(@Context HttpServletRequest request,
			@PathParam("id") int id)
	{
		Response retval = null;
		try {
			Admin.enforceThatUserIsAdmin(request);
			Credentials creds = Datastore.DS().getCredentials(id);
			creds.setQuota(Datastore.DS().getAvailableQuota(creds));
			retval = Response.status(200)
				.entity(new ObjectMapper().writeValueAsString(
					creds))
				.build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	@GET
	@Path("/groups")
	@Consumes("application/json")
	@Produces("application/json")
	public Response groupsGet(@Context HttpServletRequest request,
			@QueryParam("pageSize") @DefaultValue("25") int pgSize,
			@QueryParam("page") @DefaultValue("0") int page)
	{
		Response retval = null;
		try {
			Admin.enforceThatUserIsAdmin(request);
			Pagination pg = new Pagination(page, pgSize, false);
			String json = CrudController.toJson("groups",
				Datastore.DS().getGroups(pg));
			retval = Response.status(200).entity(json).build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	@GET
	@Path("/groups/{id}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response groupGet(@Context HttpServletRequest request,
			@QueryParam("pageSize") @DefaultValue("25") int pgSize,
			@QueryParam("page") @DefaultValue("0") int page,
			@PathParam("id") int id)
	{
		Response retval = null;
		try {
			Admin.enforceThatUserIsAdmin(request);
			Group group = Datastore.DS().getGroup(id);
			retval = Response.status(200)
				.entity(new ObjectMapper().writeValueAsString(
					group))
				.build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	@GET
	@Path("/groups/{id}/children")
	@Consumes("application/json")
	@Produces("application/json")
	public Response groupChildrenGet(@Context HttpServletRequest request,
			@QueryParam("pageSize") @DefaultValue("25") int pgSize,
			@QueryParam("page") @DefaultValue("0") int page,
			@PathParam("id") int id)
	{
		Response retval = null;
		try {
			Admin.enforceThatUserIsAdmin(request);
			Group group = Datastore.DS().getGroup(id);
			Pagination pg = new Pagination(page, pgSize, false);
			String json = CrudController.toJson("groups",
				Datastore.DS().getGroups(group));
			retval = Response.status(200).entity(json).build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	@GET
	@Path("/groups/{id}/landing")
	@Consumes("application/json")
	@Produces("application/json")
	public Response groupLandingGet(@Context HttpServletRequest request,
			@PathParam("id") int id)
	{
		Response retval = null;
		try {
			Admin.enforceThatUserIsAdmin(request);
			Group group = Datastore.DS().getGroup(id);
			String json = "{'landing':'"
				+Datastore.DS().getLandingPage(group)+"'}";
			retval = Response.status(200).entity(json).build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	@GET
	@Path("/sessions")
	@Consumes("application/json")
	@Produces("application/json")
	public Response sessionsGet(@Context HttpServletRequest request,
			@QueryParam("pageSize") @DefaultValue("25") int pgSize,
			@QueryParam("page") @DefaultValue("0") int page)
	{
		Response retval = null;
		try {
			Admin.enforceThatUserIsAdmin(request);
			Pagination pg = new Pagination(page, pgSize, false);
			String json = CrudController.toJson("sessions",
				Datastore.DS().getSessions(pg));
			retval = Response.status(200).entity(json).build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	@GET
	@Path("/sessions/{name}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response sessionGet(@Context HttpServletRequest request,
			@QueryParam("pageSize") @DefaultValue("25") int pgSize,
			@QueryParam("page") @DefaultValue("0") int page,
			@PathParam("name") String name)
	{
		Response retval = null;
		try {
			Admin.enforceThatUserIsAdmin(request);
			Session session = Datastore.DS().getSession(name);
			retval = Response.status(200)
				.entity(new ObjectMapper().writeValueAsString(
					session))
				.build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	@PUT
	@Path("/groups/{id}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response groupPut(@Context HttpServletRequest request,
			@PathParam("id") int id, String json)
	{
		Response retval = null;
		try {
			Admin.enforceThatUserIsAdmin(request);
			Group group = new ObjectMapper().readValue(json,
				Group.class);
			group.setId(id);
			logger.trace("group modified ({})", group);
			Datastore.DS().save(group);
			retval = Response.status(200)
				.entity(new ObjectMapper().writeValueAsString(
					group))
				.build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	@PUT
	@Path("/users/{id}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response credentialPut(@Context HttpServletRequest request,
			@QueryParam("group") @DefaultValue("-1") int groupId,
			@PathParam("id") int id, String json)
	{
		Response retval = null;
		try {
			Admin.enforceThatUserIsAdmin(request);
			Credentials creds = new ObjectMapper().readValue(json,
				Credentials.class);
			Group group = Datastore.DS().getGroup(groupId);
			creds = new Credentials(creds.getUsername(),
				Util.hash(creds.getPassword()), group,
				creds.getEmail(), null, false);
			creds.setId(id);
			logger.trace("user modified ({})", creds);
			new CompleteCredentials().enforce(creds);
			Datastore.DS().save(creds);
			retval = Response.status(200)
				.entity(new ObjectMapper().writeValueAsString(
					creds))
				.build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	@PUT
	@Path("/groups/{id}/landing")
	@Consumes("application/json")
	@Produces("application/json")
	public Response groupLandingPut(@Context HttpServletRequest request,
			@PathParam("id") int groupId, String json)
	{
		Response retval = null;
		try {
			Admin.enforceThatUserIsAdmin(request);
			Group group = Datastore.DS().getGroup(groupId);
			Map<String, ?> map = new ObjectMapper().readValue(json,
				new TypeReference<HashMap<String,Object>>(){});
			String url = ""+map.get(new String("landing"));
			url = (url == null) ? Security.DEF_LANDING : url;
			Datastore.DS().setLandingPage(group, url);
			retval = Response.status(200).entity(json).build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	@DELETE
	@Path("/groups/{id}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response groupDelete(@Context HttpServletRequest request,
			@PathParam("id") int id)
	{
		Response retval = null;
		try {
			Admin.enforceThatUserIsAdmin(request);
			Group group = Datastore.DS().getGroup(id);
			group.disable(true);
			logger.trace("group disabled ({})", group);
			Datastore.DS().save(group);
			retval = Response.status(200).entity("{}").build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	@DELETE
	@Path("/groups/{parentId}/children/{childId}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response groupDelete(@Context HttpServletRequest request,
			@PathParam("parentId") int parentId,
			@PathParam("childId") int childId)
	{
		Response retval = null;
		try {
			Admin.enforceThatUserIsAdmin(request);
			Group group = Datastore.DS().getGroup(childId);
			Group parent = Datastore.DS().getGroup(parentId);
			Edge edge = new Edge(group, parent);
			logger.trace("delete edge ({})", edge);
			Datastore.DS().delete(edge);
			retval = Response.status(200).entity("{}").build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	@DELETE
	@Path("/users/{id}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response credentialDelete(@Context HttpServletRequest request,
			@PathParam("id") int id)
	{
		Response retval = null;
		try {
			Admin.enforceThatUserIsAdmin(request);
			Credentials creds = Datastore.DS().getCredentials(id);
			creds.disable(true);
			logger.trace("delete user ({})", creds);
			Datastore.DS().save(creds);
			retval = Response.status(200).entity("{}").build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	@DELETE
	@Path("/sessions/{name}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response credentialDelete(@Context HttpServletRequest request,
			@PathParam("name") String name)
	{
		Response retval = null;
		try {
			Admin.enforceThatUserIsAdmin(request);
			Session session = Datastore.DS().getSession(name);
			logger.trace("delete session ({})", session);
			Datastore.DS().delete(session);
			retval = Response.status(200).entity("{}").build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	protected static void enforceThatUserIsAdmin(HttpServletRequest request)
			throws Unauthorized
	{
		try {
			Group root = new Group();
			Util.enforceSecurity(request, root);
		}
		catch (APIException|DatastoreException e) {
			throw new Unauthorized(e);
		}
	}
}
