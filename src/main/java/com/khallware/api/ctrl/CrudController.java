// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.ctrl;

import com.khallware.api.validation.Uniqueness;
import com.khallware.api.validation.Validator;
import com.khallware.api.dstore.Pagination;
import com.khallware.api.DatastoreException;
import com.khallware.api.Datastore;
import com.khallware.api.APIException;
import com.khallware.api.Unauthorized;
import com.khallware.api.domain.Tag;
import com.khallware.api.domain.Entity;
import com.khallware.api.domain.APIEntity;
import com.khallware.api.domain.Credentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * The CRUD controller abstract class.  This class constitutes the workhorse
 * of the webapp.
 *
 * @author khall
 */
public abstract class CrudController<T>
{
	private static final Logger logger = LoggerFactory.getLogger(
		CrudController.class);

	/**
	 * Create the given entity.  Enforce that the entity meets the criteria
	 * implemented in the specified validators.  Attach it to the specified
	 * tag.
	 */
	public Response handlePost(Credentials creds, T item, long tagId,
			Validator... validators)
	{
		Response retval = null;
		try {
			APIEntity entity = null;
			Tag tag = Datastore.DS().getTag(tagId);
			Util.enforceWrite(tag, creds);

			if (item instanceof APIEntity) {
				entity = (APIEntity)item;
				entity.setUser(creds.getId());
				entity.setGroup(creds.getGroup());
				Util.enforceWrite(entity, creds);
			}
			for (Validator validator : validators) {
				validator.enforce(item);
			}
			logger.trace("new Entity \"{}\" " +"({})",
				item.getClass().getSimpleName(), item);
			Datastore.DS().save(entity);

			if (tagId > Tag.ROOT && entity != null) {
				Datastore.DS().add2Tag(entity, tagId);
			}
			retval = Response.status(200)
				.entity(new ObjectMapper().writeValueAsString(
					item))
				.build();
		}
		catch (APIException|IOException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn("{}",e);
		}
		logger.info("(id={}) "+this.getClass().getSimpleName()+" POST",
			creds.getId());
		return(retval);
	}

	/**
	 * Create an entity based on the specified class with json content.
	 * If no validators are specified, enforce that the newly formed entity
	 * is unique to the system and connect it to the root tag.
	 */
	public Response handlePost(Class<T> clazz, HttpServletRequest request,
			String json, Validator... validators)
	{
		return(handlePost(clazz, request, json, Tag.ROOT, validators));
	}

	/**
	 * Create an entity based on the specified class with json content.
	 * If no validators are specified, enforce that the newly formed entity
	 * is unique to the system and connect it to the given tag.
	 */
	public Response handlePost(Class<T> clazz, HttpServletRequest request,
			String json, int tagId, Validator... validators)
	{
		return(handlePost(clazz, request, json, tagId,
			(validators.length == 0)
				? new Validator[] { new Uniqueness() }
				: validators,
			new BiConsumer[] {}));
	}

	/**
	 * Create an entity based on the specified class with json content.
	 * If any BiConsumer functions are passed in, allow them to further
	 * transform the enity. Enforce that the newly formed entity is unique
	 * to the system and connect it to the specified tag.
	 */
	public Response handlePost(Class<T> clazz, HttpServletRequest request,
			String json, int tagId, BiConsumer<T,String>... bclist)
	{
		Validator[] validators = new Validator[] { new Uniqueness() };
		return(handlePost(clazz, request, json, tagId, validators,
			bclist));
	}

	/**
	 * Create an entity based on the specified class with json content.
	 * If any BiConsumer functions are passed in, allow them to further
	 * transform the enity. Enforce that the entity meets the criteria
	 * implemented in the specified validators.  Connect the entity to the
	 * specified tag.
	 */
	public Response handlePost(Class<T> clazz, HttpServletRequest request,
			String json, int tagId, Validator[] validators,
			BiConsumer<T,String>... bclist)
	{
		Response retval = null;
		logger.trace("json ({})", json);
		try {
			Util.enforceSecurity(request);
			Credentials creds = Datastore.DS().getCredentials(
				Util.getCredentials(request));
			T item = new ObjectMapper().readValue(json, clazz);

			for (BiConsumer<T,String> consumer : bclist) {
				consumer.accept(item, json);
			}
			retval = handlePost(creds, item, tagId, validators);
		}
		catch (APIException|IOException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn("{}",e);
		}
		return(retval);
	}

	/**
	 * Read an entity typed on the specified class having this unique id.
	 */
	public Response handleGet(Class clazz, HttpServletRequest request,
			int id)
	{
		Response retval = null;
		Credentials creds = Util.makeAnonymousCreds();
		String json = null;
		try {
			Util.enforceSecurity(request);
			creds = Datastore.DS().getCredentials(
				Util.getCredentials(request));
			retval = handleGet(clazz, creds, id);
		}
		catch (APIException|IOException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn("{}",e);
		}
		logger.info("(id={}) GET "+this.getClass().getSimpleName()
			+" ({})", creds.getId(), id);
		return(retval);
	}

	/**
	 * Update.
	 */
	public Response handlePut(Class<T> clazz, HttpServletRequest request,
			String json, int id, long tagId,
			BiConsumer<T,String>... callbacks)
	{
		Response retval = null;
		Credentials creds = Util.makeAnonymousCreds();
		logger.trace("json ({})", json);
		try {
			APIEntity entity = null;
			Util.enforceSecurity(request);
			creds = Datastore.DS().getCredentials(
				Util.getCredentials(request));
			Tag tag = Datastore.DS().getTag(tagId);
			Util.enforceWrite(tag, creds);
			T item = new ObjectMapper().readValue(json, clazz);

			if (item instanceof APIEntity) {
				entity = (APIEntity)item;
				entity.setUser(creds.getId());
				entity.setGroup(creds.getGroup());
				entity.setId(id);
				Util.enforceWrite(entity, creds);
			}
			for (BiConsumer<T,String> consumer : callbacks){
				consumer.accept(item, json);
			}
			logger.info("updated APIEntity \""+clazz.getSimpleName()
				+"\" ("+item+")");
			Datastore.DS().save(entity);

			if (tagId > 0 && entity != null) {
				Datastore.DS().add2Tag(entity, tagId);
			}
			retval = handleGet(clazz, creds, id);
		}
		catch (APIException|IOException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn("{}",e);
		}
		logger.info("(id={}) PUT "+this.getClass().getSimpleName()
			+" ({})", creds.getId(), id);
		return(retval);
	}

	/**
	 * Delete.
	 */
	public Response handleDelete(Class clazz, HttpServletRequest request,
			int id, int tagId, Consumer<APIEntity>... callbacks)
	{
		Response retval = null;
		Credentials creds = Util.makeAnonymousCreds();
		try {
			APIEntity entity = null;
			Util.enforceSecurity(request);
			creds = Datastore.DS().getCredentials(
				Util.getCredentials(request));
			try {
				retval = handleGet(clazz, creds, id);
			}
			catch (Unauthorized e) {
				logger.trace(""+e, e);
				logger.warn("{}",e);
			}
			if ((entity = Datastore.DS().get(clazz, id)) != null) {
				Util.enforceRead(entity, creds);
				Util.enforceWrite(entity, creds);

				if (tagId > 0) {
					Datastore.DS().deleteEntityTags(
						entity, tagId);

					if (Datastore.DS().getTags(entity,
							new Pagination(), creds)
							.size() == 0) {
						Util.enforceWrite(entity,creds);
						Datastore.DS().delete(entity);
						callBack(callbacks, entity);
					}
				}
				else {
					Datastore.DS().deleteEntityTags(
						entity, creds);
					Util.enforceWrite(entity,creds);
					Datastore.DS().delete(entity);
					callBack(callbacks, entity);
				}
			}
		}
		catch (APIException|IOException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn("{}",e);
		}
		logger.info("(id={}) DELETE "+this.getClass().getSimpleName()
			+" ({})", creds.getId(), id);
		return(retval);
	}

	/**
	 * Read multiple (search).  Will filter tag for the given tagId
	 * or not for no tag (ie tagId < 0).
	 */
	public Response handleGet(Class clazz, HttpServletRequest request,
			Pagination pg, int tagId, String name)
	{
		Response retval = null;
		Credentials creds = Util.makeAnonymousCreds();
		try {
			Datastore dstore = Datastore.DS();
			Util.enforceSecurity(request);
			Tag tag = dstore.getTag(tagId);
			creds = dstore.getCredentials(
				Util.getCredentials(request));
			tag = (tag == null && tagId >= 0) ? new Tag() : tag;

			if (pg.returnCount()) {
				if (tag == null) {
					dstore.get(clazz, pg, creds);
				}
				else {
					dstore.get(clazz, tag, pg, creds);
				}
			}
			retval = Response
				.status(200)
				.entity(pg.returnCount()
					? "{ \"count\" : "+pg.getCount()+" }"
					: toJson(this.getClass(), (tag == null)
						? dstore.get(clazz, pg, creds)
						: dstore.get(clazz, tag, pg,
							creds)))
				.build();
		}
		catch (APIException|IOException|DatastoreException e) {
			retval = Util.failRequest(e);
			logger.trace(""+e, e);
			logger.warn("{}",e);
		}
		logger.info("(id={}) GET "+this.getClass().getSimpleName(),
			creds.getId());
		return(retval);
	}

	public static String toJson(Class clazz, List<?> list)
			throws IOException
	{
		return(toJson(clazz.getSimpleName().toLowerCase(), list));
	}

	public static String toJson(String key, List<?> list)
			throws IOException
	{
		StringBuilder retval = new StringBuilder();
		retval.append("{\""+key+"\":");
		retval.append(new ObjectMapper().writeValueAsString(list));
		retval.append("}");
		return(""+retval);
	}

	protected Response handleGet(Class clazz, Credentials creds, long id)
			throws APIException, IOException, DatastoreException
	{
		Response retval = null;
		APIEntity entity = Datastore.DS().get(clazz, id);
		String json = new ObjectMapper().writeValueAsString(entity);
		Util.enforceRead(entity, creds);
		retval = Response.status(200).entity(json).build();
		return(retval);
	}

	protected void callBack(Consumer<APIEntity>[] callbacks,
			APIEntity entity) throws APIException
	{
		for (Consumer<APIEntity> consumer : callbacks) {
			consumer.accept(entity);
		}
	}
}
