// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.dstore;

import com.khallware.api.DatastoreException;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.Entity;
import com.khallware.api.domain.Tag;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The CrudChain.  This class implements the chain of command pattern to be
 * used by the datastore, specifically for CRUD (create, read, update, and
 * delete) operations.
 *
 * @author khall
 */
public abstract class CrudChain<T extends Entity>
{
	private static final Logger logger = LoggerFactory.getLogger(
		CrudChain.class);

	protected CrudChain next = null;

	public CrudChain next(CrudChain next)
	{
		this.next = next;
		logger.trace("set next chain to ({})", next);
		return(next);
	}

	public boolean canHandle(Class clazz)
	{
		return(onGetClass().isAssignableFrom(clazz));
	}

	public T create(T entity) throws DatastoreException
	{
		T retval = null;

		if (entity != null && canHandle(entity.getClass())) {
			retval = onCreate(entity);
			logger.trace("create() with ({})", this);
		}
		else if (next != null) {
			logger.trace("skipping chain link ({})", this);
			retval = (T)next.create(entity);
		}
		return(retval);
	}

	public T read(Class clazz, long id) throws DatastoreException
	{
		T retval = null;

		if (canHandle(clazz)) {
			retval = onRead(id);
			logger.trace("read() with ({})", this);
		}
		else if (next != null) {
			logger.trace("skipping chain link ({})", this);
			retval = (T)next.read(clazz, id);
		}
		return(retval);
	}

	public List<T> read(Class clazz, Pagination pg, Credentials creds)
			throws DatastoreException
	{
		List<T> retval = null;

		if (canHandle(clazz)) {
			retval = (List<T>)onRead(pg, creds);
			logger.trace("read(page) with ({})", this);
		}
		else if (next != null) {
			logger.trace("skipping chain link ({})", this);
			retval = (List<T>)next.read(clazz, pg, creds);
		}
		return(retval);
	}

	public List<T> read(Class clazz, Tag tag, Pagination pg,
			Credentials creds) throws DatastoreException
	{
		List<T> retval = null;

		if (canHandle(clazz)) {
			retval = (List<T>)onRead(tag, pg, creds);
			logger.trace("read(tag, page) with ({})", this);
		}
		else if (next != null) {
			logger.trace("skipping chain link ({})", this);
			retval = (List<T>)next.read(clazz, tag, pg, creds);
		}
		return(retval);
	}

	public List<T> read(Pagination pg, Credentials creds, T pattern)
			throws DatastoreException
	{
		List<T> retval = null;
		Class clazz = (pattern != null) ? pattern.getClass() : null;

		if (pattern != null && canHandle(clazz)) {
			retval = (List<T>)onRead(pg, creds, pattern);
			logger.trace("read(page) with ({})", this);
		}
		else if (pattern != null && next != null) {
			logger.trace("skipping chain link ({})", this);
			retval = (List<T>)next.read(clazz, pg, creds);
		}
		return(retval);
	}

	public List<T> read(T pattern) throws DatastoreException
	{
		List<T> retval = null;

		if (pattern != null && canHandle(pattern.getClass())) {
			retval = (List<T>)onRead(pattern);
			logger.trace("read(pattern) with ({})", this);
		}
		else if (next != null) {
			logger.trace("skipping chain link ({})", this);
			retval = (List<T>)next.read(pattern);
		}
		return(retval);
	}

	public T update(T entity) throws DatastoreException
	{
		T retval = null;

		if (entity != null && canHandle(entity.getClass())) {
			retval = (T)onUpdate(entity);
			logger.trace("update() with ({})", this);
		}
		else if (next != null) {
			logger.trace("skipping chain link ({})", this);
			retval = (T)next.update(entity);
		}
		return(retval);
	}

	public T delete(T entity) throws DatastoreException
	{
		T retval = null;

		if (entity != null && canHandle(entity.getClass())) {
			retval = (T)onDelete(entity);
			logger.trace("delete() with ({})", this);
		}
		else if (next != null) {
			logger.trace("skipping chain link ({})", this);
			retval = (T)next.delete(entity);
		}
		return(retval);
	}

	public T delete(Class clazz, long id) throws DatastoreException
	{
		T retval = null;

		if ((retval = (T)read(clazz, id)) != null) {
			retval = delete((T)read(clazz, id));
		}
		else {
			String type = clazz.getSimpleName().toLowerCase();
			logger.warn("no "+type+" id=\"{}\"",id);
		}
		return(retval);
	}

	public T add2Tag(T entity, Tag tag) throws DatastoreException
	{
		T retval = null;

		if (entity != null && canHandle(entity.getClass())) {
			retval = (T)onAdd2Tag(entity, tag);
			logger.trace("add2Tag() with ({})", this);
		}
		else if (next != null) {
			logger.trace("skipping chain link ({})", this);
			retval = (T)next.add2Tag(entity, tag);
		}
		return(retval);
	}

	public List<Tag> getTags(T entity, Pagination pg)
			throws DatastoreException
	{
		List<Tag> retval = null;

		if (entity != null && canHandle(entity.getClass())) {
			retval = onGetTags(entity, pg);
			logger.trace("getTags() with ({})", this);
		}
		else if (next != null) {
			logger.trace("skipping chain link ({})", this);
			retval = next.getTags(entity, pg);
		}
		return(retval);
	}

	public T deleteFromTags(T entity, Tag tag) throws DatastoreException
	{
		T retval = null;

		if (entity != null && canHandle(entity.getClass())) {
			retval = (T)onDeleteFromTags(entity, tag);
			logger.trace("deleteFromTags() with ({})", this);
		}
		else if (next != null) {
			logger.trace("skipping chain link ({})", this);
			retval = (T)next.deleteFromTags(entity, tag);
		}
		return(retval);
	}

	/**
	 * Override this method to implement "C" creating of the entity.
	 */
	protected T onCreate(T entity) throws DatastoreException
	{
		return((T)null);
	}

	/**
	 * Override this method to implement "R" reading of the entity.
	 */
	protected T onRead(long id) throws DatastoreException
	{
		return((T)null);
	}

	/**
	 * Override this method to implement "R" reading of the entity.
	 */
	protected List<T> onRead(Pagination pg, Credentials creds)
			throws DatastoreException
	{
		return(onRead(pg, creds, (T)null));
	}

	/**
	 * Override this method to implement "R" reading of the entity.
	 */
	protected List<T> onRead(Tag tag, Pagination pg, Credentials creds)
			throws DatastoreException
	{
		return((List<T>)null);
	}

	/**
	 * Override this method to implement "R" reading of the entity.
	 */
	protected List<T> onRead(T pattern) throws DatastoreException
	{
		return(onRead(new Pagination(), null, pattern));
	}

	/**
	 * Override this method to implement "R" reading of the entity.
	 */
	protected List<T> onRead(Pagination pg, Credentials creds, T pattern)
			throws DatastoreException
	{
		return((List<T>)null);
	}

	/**
	 * Override this method to implement "U" updating of the entity.
	 */
	protected T onUpdate(T entity) throws DatastoreException
	{
		return((T)null);
	}

	/**
	 * Override this method to implement "D" deleting of the entity.
	 */
	protected T onDelete(T entity) throws DatastoreException
	{
		return((T)null);
	}

	protected T onAdd2Tag(T entity, Tag tag) throws DatastoreException
	{
		return((T)null);
	}

	protected T onDeleteFromTags(T entity, Tag t) throws DatastoreException
	{
		return((T)null);
	}

	protected List<Tag> onGetTags(T entity, Pagination pg)
			throws DatastoreException
	{
		return(Collections.<Tag>emptyList());
	}

	/**
	 * Override this method to return class with a more specific class.
	 */
	protected Class onGetClass()
	{
		return(Void.class);
	}

	@Override
	public String toString()
	{
		return(onGetClass().getSimpleName()+" chain");
	}
}
