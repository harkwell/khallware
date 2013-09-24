// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.ctrl;

import com.khallware.api.validation.SecurityPolicyFactory;
import com.khallware.api.validation.PolicyViolation;
import com.khallware.api.dstore.Pagination;
import com.khallware.api.DatastoreException;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.Session;
import com.khallware.api.domain.Group;
import com.khallware.api.Datastore;
import com.khallware.api.APIException;
import com.khallware.api.ServletContainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.QueryParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.GET;
import java.net.URI;
import java.util.UUID;
import java.util.List;
import java.util.Properties;
import java.io.IOException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Path("/security")
public class Security
{
	private static final Logger logger = LoggerFactory.getLogger(
		Security.class);
	public static final long TIMEOUTMS = Session.DEF_TIMEOUT;
	public static final String COOKIE = "khallware";
	public static final String DEF_LANDING = "/apis/v1/static/index.html";

	@FunctionalInterface
	public interface Policy<T>
	{
		public void enforce(T item) throws PolicyViolation;
	}

	/**
	 * Simple login html page.
	 */
	@GET
	@Path("/login.html")
	@Consumes("text/html")
	@Produces("text/html")
	public Response handleLoginGet()
	{
		StringBuilder retval = new StringBuilder();
		StringBuilder cookie = new StringBuilder()
			.append(COOKIE+"=;")
			.append("expires=Thu, 01 Jan 1970 00:00:00 GMT")
			.append("Path=/;");
		retval.append("<!DOCTYPE html>\n");
		retval.append("<html>\n");
		retval.append("\t<head>\n");
		retval.append("\t\t<title>khallware: login</title>\n");
		retval.append("\t</head>\n");
		retval.append("\t<body>\n");
		retval.append("\t\t<form method=\"post\" ");
		retval.append("action=\"/apis/v1/security/login.html\">\n");
		retval.append("\t\t\tUsername: <input type=\"text\" ");
		retval.append("name=\"username\"><br>\n");
		retval.append("\t\t\tPassword: <input type=\"password\" ");
		retval.append("name=\"password\"><br>\n");
		retval.append("\t\t\t<input type=\"submit\" ");
		retval.append("value=\"Submit\">\n");
		retval.append("\t\t</form>\n");
		retval.append("\t</body>\n");
		retval.append("</html>\n");
		return(Response.status(200)
			.entity(retval.toString())
			.cookie(NewCookie.valueOf(""+cookie))
			.build());
	}

	/**
	 * Login action.
	 */
	@POST
	@Path("/login")
	@Produces("text/plain")
	public Response handleLoginPost(@Context HttpServletRequest request,
			@QueryParam(value="username") String username,
			@QueryParam(value="password") String password)
	{
		return(handleLoginRequest(request, username, password, false));
	}

	/**
	 * Login action from browser html form post.
	 */
	@POST
	@Path("/login.html")
	@Produces("text/plain")
	public Response handleFormPost(@Context HttpServletRequest request,
			@FormParam(value="username") String username,
			@FormParam(value="password") String password)
	{
		return(handleLoginRequest(request, username, password, true));
	}

	/**
	 * Logout action.
	 */
	@POST
	@Path("/logout")
	@Produces("text/plain")
	public Response handleLogoutPost(@Context HttpServletRequest request)
	{
		return(handleLogoutRequest(request));
	}

	/**
	 * Logout action from browser html form post.
	 */
	@GET
	@Path("/logout.html")
	@Produces("text/plain")
	public Response handleFormPost(@Context HttpServletRequest request)
	{
		return(handleLogoutRequest(request));
	}

	/**
	 * Simple register html page.
	 */
	@GET
	@Path("/register.html")
	@Consumes("text/html")
	@Produces("text/html")
	public Response handleRegisterGet(@Context HttpServletRequest request)
	{
		StringBuilder retval = new StringBuilder();
		String ctxtPath = ""+request.getContextPath();
		htmlHeader(retval);
		retval.append("\t\t<form method=\"post\" ");
		retval.append("action=\"/apis/v1/security/register.html\">\n");
		retval.append("\t\t\tUsername: <input type=\"text\" ");
		retval.append("name=\"username\"><br>\n");
		retval.append("\t\t\tPassword: <input type=\"password\" ");
		retval.append("name=\"password\"><br>\n");
		retval.append("\t\t\tVerfiy: <input type=\"password\" ");
		retval.append("name=\"verify\"><br>\n");
		retval.append("\t\t\tEmail: <input type=\"text\" ");
		retval.append("name=\"email\"><br>\n");
		retval.append("\t\t\tCaptcha: <input type=\"text\" ");
		retval.append("name=\"captcha\"><br>\n");
		retval.append("\t\t\t<input type=\"submit\" ");
		retval.append("value=\"Submit\">\n");
		retval.append("\t\t</form>\n");
		retval.append("\t<img src=\""+ctxtPath);
		retval.append("/media/captcha.png\">\n");
		retval.append("\t</body>\n");
		retval.append("</html>\n");
		return(Response.status(200).entity(retval.toString()).build());
	}

	/**
	 * Change password html page.
	 */
	@GET
	@Consumes("text/html")
	@Produces("text/html")
	@Path("/change_passwd.html")
	public Response handlePasswdChgGet(@Context HttpServletRequest request)
	{
		StringBuilder retval = new StringBuilder();
		htmlHeader(retval);
		String ctxtPath = ""+request.getContextPath();
		retval.append("\t\t<form method=\"post\" action=");
		retval.append("\"/apis/v1/security/change_passwd.html\">\n");
		retval.append("\t\t\tUsername: <input type=\"text\" ");
		retval.append("name=\"username\"><br>\n");
		retval.append("\t\t\tOld Password: <input type=\"password\" ");
		retval.append("name=\"old_password\"><br>\n");
		retval.append("\t\t\tNew Password: <input type=\"password\" ");
		retval.append("name=\"new_password\"><br>\n");
		retval.append("\t\t\tVerfiy: <input type=\"password\" ");
		retval.append("name=\"verify\"><br>\n");
		retval.append("\t\t\t<input type=\"submit\" ");
		retval.append("value=\"Submit\">\n");
		retval.append("\t\t</form>\n");
		retval.append("\t</body>\n");
		retval.append("</html>\n");
		return(Response.status(200).entity(retval.toString()).build());
	}

	/**
	 * Register action.
	 */
	@GET
	@Path("/register/{regikey}.html")
	@Produces("text/html")
	public Response handleRegisterPost(@Context HttpServletRequest request,
			@PathParam(value="regikey") String regikey)
	{
		Response retval = null;
		try {
			Credentials creds = Datastore.DS().getCredentials(
				UUID.fromString(regikey));

			if (creds != null) {
				creds.setRegikey(null);
				creds.disable(false);
				Datastore.DS().save(creds);
				retval = Response.status(200)
					.entity("successfully registered user "
						+creds.getUsername())
					.build();
				logger.info("Registration verified for \"{}\"",
					creds.getUsername());
				Util.sendNewActiveUserNotification(creds);
			}
			else {
				retval = Response.status(404)
					.entity("")
					.build();
			}
		}
		catch (DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	/**
	 * Register action.
	 */
	@POST
	@Path("/register")
	@Produces("text/plain")
	public Response handleRegisterPost(@Context HttpServletRequest request,
			@FormParam(value="username") String username,
			@FormParam(value="password") String password,
			@FormParam(value="verify") String verify,
			@FormParam(value="email") String email,
			@FormParam(value="captcha") String captcha)
	{
		return(handleRegisterRequest(request, username, password,
			verify, email, captcha));
	}

	/**
	 * Register action from browser html form post.
	 */
	@POST
	@Path("/register.html")
	@Produces("text/plain")
	public Response handleFormPost(@Context HttpServletRequest request,
			@FormParam(value="username") String username,
			@FormParam(value="password") String password,
			@FormParam(value="verify") String verify,
			@FormParam(value="email") String email,
			@FormParam(value="captcha") String captcha)
	{
		return(handleRegisterRequest(request, username, password,
				verify, email, captcha));
	}

	/**
	 * Change password action from browser html form post.
	 */
	@POST
	@Produces("text/plain")
	@Path("/change_passwd.html")
	public Response handleFormPost(@Context HttpServletRequest request,
			@FormParam(value="username") String username,
			@FormParam(value="old_password") String oldPassword,
			@FormParam(value="new_password") String newPassword,
			@FormParam(value="verify") String verify)
	{
		return(handleChangePasswdRequest(request, username, oldPassword,
				newPassword, verify));
	}

	/**
	 * List my groups.
	 */
	@GET
	@Path("/groups")
	@Produces("application/json")
	public Response handleGetGroups(@Context HttpServletRequest request)
	{
		Response retval = null;
		try {
			Credentials creds = Datastore.DS().getCredentials(
				Util.getCredentials(request));
			List<Group> groups = Datastore.DS().getGroups(creds);
			String json = CrudController.toJson(Group.class,groups);
			retval = Response.status(200).entity(json).build();
			logger.info("Groups requested for \"{}\"",
				creds.getUsername());
		}
		catch (APIException|IOException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	/**
	 * Enforce validity of the session or login.
	 */
	@GET
	@Path("/details")
	@Consumes("application/json")
	@Produces("application/json")
	public Response detailsGet(@Context HttpServletRequest request)
	{
		Response retval = null;
		try {
			String sessName = Util.getSessionName(request);
			Session session = Datastore.DS().getSession(sessName);
			String json = new ObjectMapper().writeValueAsString(
				session);
			retval = Response.status(200).entity(json).build();
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	protected Response handleLoginRequest(HttpServletRequest request,
			String username, String password, boolean redirect)
	{
		Response retval = null;
		logger.info("Login request from \"{}\"", username);
		try {
			Credentials creds = new Credentials(username,
				Util.hash(password), null);
			Util.enforceSecurity(creds);
			String sessName = Util.getSessionName(request);
			Session session = Datastore.DS().getSession(sessName);
			creds = Datastore.DS().getCredentials(username);

			if (session == null) {
				session = Util.createNewSession(username);
				sessName = session.getName();
			}
			Datastore.DS().save(session);
			Datastore.DS().noteLogin(creds,
				request.getHeader("User-Agent"));
			StringBuilder cookie = new StringBuilder()
				.append(COOKIE+"="+sessName+";")
				.append("Path=/;");
			retval = Response.status((redirect) ? 303 : 200)
				.entity(session.getName())
				.header((redirect) ? "Location" : "AltLocation",
					determineLocation(creds))
				.header("Cache-Control",
					"private, max-age=0, no-cache")
				.cookie(NewCookie.valueOf(""+cookie))
				.build();
		}
		catch (Exception e) {
			retval = Response.status(303)
				.header("Location", "/apis/login.html")
				.build();
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	protected Response handleLogoutRequest(HttpServletRequest request)
	{
		Response retval = Response.status(303)
			.entity("{}")
			.header("Location", "/apis/login.html")
			.build();
		Session session = null;
		try {
			Credentials creds = Util.getCredentials(request);
			String sessName = Util.getSessionName(request);
			logger.info("Logout request from \"{}\" session \"{}\"",
				creds.getUsername(), sessName);

			if ((session = Datastore.DS().getSession(sessName))
					!= null) {
				Datastore.DS().delete(session);
			}
		}
		catch (Exception e) {
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	protected String determineLocation(Group group)
			throws DatastoreException
	{
		return(Datastore.DS().getLandingPage(group));
	}

	protected String determineLocation(Credentials creds)
			throws DatastoreException
	{
		String retval = null;

		if ((retval = determineLocation(creds.getGroup())) != null) {
			logger.trace("users primary group has specific page");
		}
		else {
			for (Group group : Datastore.DS().getGroups(creds)) {
				if ((retval=determineLocation(group)) != null) {
					logger.trace("found specific page");
					break;
				}
			}
		}
		retval = (retval == null) ? DEF_LANDING : retval;
		logger.trace("landing page is \"{}\"", retval);
		return(retval);
	}

	protected void enforcePolicies(Credentials creds, String verify,
			String captcha) throws PolicyViolation
	{
		SecurityPolicyFactory.captcha(
			ServletContainer.getCaptcha()).enforce(captcha);
		SecurityPolicyFactory.username().enforce(creds);
		SecurityPolicyFactory.password(verify).enforce(creds);
	}

	protected static StringBuilder htmlHeader(StringBuilder retval)
	{
		retval.append("<!DOCTYPE html>\n");
		retval.append("<html>\n");
		retval.append("\t<head>\n");
		retval.append("\t\t<title>khallware: register</title>\n");
		retval.append("\t</head>\n");
		retval.append("\t<body>\n");
		return(retval);
	}

	protected Response handleRegisterRequest(HttpServletRequest request,
			String username, String password, String verify,
			String email, String captcha)
	{
		Response retval = Response.status(303)
			.header("Location", "/apis/login.html").build();
		logger.info("New user registration request: \"{}\" \"{}\"",
			email, username);
		try {
			Credentials creds = new Credentials(username,
				Util.hash(password), null, email,
				""+UUID.randomUUID(), true);
			Group group = new Group(username, "default");

			enforcePolicies(creds, verify, captcha);
			Datastore.DS().save(group);
			creds.setGroup(group);
			Datastore.DS().save(creds);
			Util.sendRegistrationEmail(creds);
		}
		catch (Exception e) {
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	protected Response handleChangePasswdRequest(HttpServletRequest request,
			String username, String oldPassword, String newPassword,
			String verify)
	{
		Response retval = null;
		logger.info("Password change request from: \"{}\"", username);
		try {
			Credentials creds = new Credentials(username,
				Util.hash(oldPassword), null);
			Util.enforceSecurity(creds);
			creds = Datastore.DS().getCredentials(username);
			creds.setPassword(Util.hash(newPassword));
			Datastore.DS().save(creds);
			retval = Response.status(303)
				.header("Location", determineLocation(creds))
				.build();
			Util.sendPasswordChangeEmail(creds);
		}
		catch (Exception e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}
}
