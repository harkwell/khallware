// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.ctrl;

import com.khallware.api.APIException;
import com.khallware.api.ContactFactory;
import com.khallware.api.DatastoreException;
import com.khallware.api.dstore.Pagination;
import com.khallware.api.Unauthorized;
import com.khallware.api.Datastore;
import com.khallware.api.domain.Tag;
import com.khallware.api.domain.Contact;
import com.khallware.api.domain.Credentials;
import com.khallware.api.validation.Validator;
import com.khallware.api.validation.UniqueContact;
import com.khallware.api.validation.CompleteContact;
import com.khallware.api.validation.Add2TagDuplicateHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/contacts")
public class Contacts extends CrudController<Contact>
{
	public static final long MAX_VCARD_ELMTS = 1024;
	private static final Logger logger = LoggerFactory.getLogger(
		Contacts.class);

	/**
	 * Create a new contact from text/vcard.
	 */
	@POST
	@Consumes("text/vcard")
	@Produces("text/vcard")
	public Response handlePost(@Context HttpServletRequest request,
			@QueryParam("tagId") @DefaultValue("0") int tagId,
			String vcard)
	{
		Response retval = null;
		ObjectMapper mapper = new ObjectMapper();
		List<Contact> contacts = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		try {
			for (Contact contact : ContactFactory.make(vcard)) {
				String jcard = Contact.vcardToJson(
					Contact.parse(contact.getVcard()));
				String json = new StringBuilder()
					.append("{\"jcard\":[")
					.append(jcard)
					.append("]}")
					.toString();
				handlePost(Contact.class, request, json, tagId,
					getValidators(tagId),
					(i,j) -> fixJcard(i, j));
				contacts.add(contact);
			}
			sb.append("{ contacts : ")
				.append(mapper.writeValueAsString(contacts))
				.append("}");
		}
		catch (Exception e) {
			logger.trace(""+e, e);
			logger.warn("{}",""+e);
		}
		retval = Response.status(200).entity(sb.toString()).build();
		return(retval);
	}

	/**
	 * Create a new contact.
	 */
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response handleJsonPost(@Context HttpServletRequest request,
			@QueryParam("tagId") @DefaultValue("0") int tagId,
			String json)
	{
		return(handlePost(Contact.class, request, json, tagId,
			getValidators(tagId), (i,j) -> fixJcard(i, j)));
	}

	/**
	 * Read a specific contact.
	 */
	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleGet(Contact.class, request, id));
	}

	/**
	 * Read a specific contact.
	 */
	@GET
	@Path("/{id}")
	@Produces("text/vcard")
	public Response handleMimeGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleVcardGet(request, id));
	}

	/**
	 * Read a specific contact.
	 */
	@GET
	@Path("/{id}.vcf")
	@Produces("text/vcard")
	public Response handleExtGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		return(handleVcardGet(request, id));
	}

	public Response handleVcardGet(@Context HttpServletRequest request,
			@PathParam(value="id") int id)
	{
		Response retval = null;
		try {
			Contact contact = null;
			String xml = "";
			Util.enforceSecurity(request);

			if ((contact = Datastore.DS().getContact(id)) != null) {
				xml = contact.getVcard();
			}
			retval = Response.status(200).entity(xml).build();
		}
		catch (APIException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn("{}",""+e);
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
		return(handlePut(Contact.class, request, json, id, tagId,
			(i,j) -> fixJcard(i,j)));
	}

	@DELETE
	@Path(value="/{id}")
	@Produces("application/json")
	public Response handleDelete(@Context HttpServletRequest request,
			@PathParam(value="id") int id,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		return(handleDelete(Contact.class, request, id, tagId));
	}

	/**
	 * List contacts.
	 */
	@GET
	@Produces("application/json")
	public Response handleGet(@Context HttpServletRequest request,
			@QueryParam("page") @DefaultValue("0") int page,
			@QueryParam("pageSize") @DefaultValue("-1") int pgSize,
			@QueryParam("count") @DefaultValue("false") boolean cnt,
			@QueryParam("name") String name,
			@QueryParam("tagId") int tagId,
			@QueryParam("sort") @DefaultValue("name") String sort,
			@QueryParam(value="user") String user)
	{
		Pagination pg = new Pagination(page, pgSize, cnt);
		pg.setSortBy(sort);
		return(handleGet(Contact.class, request, pg, tagId, name));
	}

	/**
	 * Read a specific contact.
	 */
	@GET
	@Path("/cards.vcf")
	@Produces("text/vcard")
	public Response handleMultiExtGet(@Context HttpServletRequest request,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		return(handleVcardGet(request, "", "", "", tagId));
	}

	/**
	 * List contacts as vcard list.
	 */
	@GET
	@Produces("text/vcard")
	public Response handleVcardGet(@Context HttpServletRequest request,
			@QueryParam("name") String name,
			@QueryParam(value="user") String user,
			@QueryParam("sort") @DefaultValue("name") String sort,
			@QueryParam("tagId") @DefaultValue("0") int tagId)
	{
		Response retval = null;
		try {
			Datastore dstore = Datastore.DS();
			List<VCard> vcards = new ArrayList<>();
			Pagination pg = new Pagination(1,MAX_VCARD_ELMTS,false);
			Credentials creds = Util.getCredentials(request);
			VCard vcard = null;
			Util.enforceSecurity(request);
			Tag tag = dstore.getTag(tagId);
			pg.setSortBy(sort);

			for (Contact c : dstore.getContacts(tag, pg, creds)) {
				if ((vcard = Contact.parse(c.getVcard()))
						== null) {
					logger.warn("vcard parse error ({})",
						c.getVcard());
					continue;
				}
				vcards.add(vcard);
			}
			CharArrayWriter writer = new CharArrayWriter();
			Ezvcard.write(vcards).go(writer);
			retval = Response.status(200).entity(""+writer).build();
		}
		catch (APIException|IOException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn("{}",""+e);
		}
		return(retval);
	}

	protected static void fixJcard(Contact contact, String json)
	{
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jcard = mapper.readValue(json,
				JsonNode.class).get("jcard");

			if (jcard != null && !(""+jcard).isEmpty()) {
				Contact.updateContact(
					contact, Contact.parseJson(""+jcard));
			}
			else {
				Contact.updateContact(
					contact, Contact.parseJson(
						Contact.toJcard(contact)));
			}
		}
		catch (Exception e) {
			logger.trace(""+e, e);
			logger.warn("{}",""+e);
		}
	}

	protected static Validator[] getValidators(long tagId)
	{
		Validator[] retval = new Validator[] {
			new CompleteContact(),
			new UniqueContact(new Add2TagDuplicateHandler(tagId))
		};
		return(retval);
	}
}
