// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.dstore;

import com.khallware.api.Datastore.Wrapper;
import com.khallware.api.DatastoreException;
import com.khallware.api.dstore.Operator.Operation;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.ContactTags;
import com.khallware.api.domain.Tag;
import com.khallware.api.domain.Contact;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.dao.Dao;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DContacts extends APICrudChain<Contact>
{
	private static final Logger logger = LoggerFactory.getLogger(
		DContacts.class);

	@Override
	public Class onGetClass()
	{
		return(Contact.class);
	}

	@Override
	public Contact onCreate(final Contact contact)
			throws DatastoreException
	{
		Operator.<Contact>perform((dao) -> {
			contact.preSave();
			dao.createOrUpdate(contact);
		}, Contact.class);
		return(contact);
	}

	@Override
	public Contact onRead(final long id) throws DatastoreException
	{
		final Wrapper<Contact> retval = new Wrapper<>();
		Operator.<Contact>perform((dao) -> {
			retval.item = dao.queryForId((int)id);
		}, Contact.class);
		return(retval.item);
	}

	@Override
	public List<Contact> onRead(final Pagination pg,
			final Credentials creds, final Contact pattern)
			throws DatastoreException
	{
		final Wrapper<List<Contact>> retval = new Wrapper<>();
		Operator.<Contact>perform((dao) -> {
			QueryBuilder<Contact, Integer> qb = dao.queryBuilder()
				.setCountOf(pg.returnCount())
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			retval.item = new ArrayList<Contact>();
			filterOnMode(qb.where(), creds);
			retval.count = pg.returnCount() ? qb.countOf() : -1;
			retval.item.addAll((pattern == null)
				? qb.query()
				: dao.queryForMatchingArgs(pattern));
		}, Contact.class);
		pg.setCount(retval.count);
		return(retval.item);
	}

	@Override
	public List<Contact> onRead(final Tag tag, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		final List<Contact> retval = new ArrayList<>();
		Operator.<ContactTags>perform((dao) -> {
			QueryBuilder<ContactTags, Integer> qb =
				dao.queryBuilder()
					.setCountOf(pg.returnCount())
					.offset(pg.calcCursorIndex())
					.limit(pg.getPageSize());
			qb.where().eq(ContactTags.COL_TAG, tag);

			if (!pg.returnCount()) {
				for (ContactTags ct : qb.query()) {
					retval.add(ct.getContact());
				}
			}
			pg.setCount(pg.returnCount() ? qb.countOf() : -1);
		}, ContactTags.class);
		return(retval);
	}

	@Override
	public Contact onUpdate(final Contact contact)
			throws DatastoreException
	{
		Operator.<Contact>perform((dao) -> dao.update(contact),
			Contact.class);
		return(contact);
	}

	@Override
	public Contact onDelete(final Contact contact)
			throws DatastoreException
	{
		Operator.<Contact>perform((dao) -> dao.delete(contact),
			Contact.class);
		return(contact);
	}

	@Override
	public Contact onAdd2Tag(final Contact contact, final Tag tag)
			throws DatastoreException
	{
		Operator.<ContactTags>perform((dao) -> {
			dao.createOrUpdate(new ContactTags(contact, tag));
		}, ContactTags.class);
		return(contact);
	}

	@Override
	public Contact onDeleteFromTags(final Contact contact, final Tag tag)
			throws DatastoreException
	{
		Operator.<ContactTags>perform((dao) -> {
			dao.delete(new ContactTags(contact, tag));
		}, ContactTags.class);
		return(contact);
	}

	@Override
	public List<Tag> onGetTags(final Contact contact, final Pagination pg)
			throws DatastoreException
	{
		final List<Tag> retval = new ArrayList<>();
		Operator.<ContactTags>perform((dao) -> {
			QueryBuilder<ContactTags, Integer> qb =
				dao.queryBuilder()
					.offset(pg.calcCursorIndex())
					.limit(pg.getPageSize());
			qb.where().eq(ContactTags.COL_BLOG, contact);

			for (ContactTags ct : qb.query()) {
				retval.add(ct.getTag());
			}
		}, ContactTags.class);
		return(retval);
	}
}
