// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.dstore;

import com.khallware.api.Datastore.Wrapper;
import com.khallware.api.DatastoreException;
import com.khallware.api.dstore.Operator.Operation;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.SoundTags;
import com.khallware.api.domain.Tag;
import com.khallware.api.domain.Sound;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.dao.Dao;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DSounds extends APICrudChain<Sound>
{
	private static final Logger logger = LoggerFactory.getLogger(
		DSounds.class);

	@Override
	public Class onGetClass()
	{
		return(Sound.class);
	}

	@Override
	public Sound onCreate(final Sound sound)
			throws DatastoreException
	{
		Operator.<Sound>perform((dao) -> {
			sound.preSave();
			dao.createOrUpdate(sound);
		}, Sound.class);
		return(sound);
	}

	@Override
	public Sound onRead(final long id) throws DatastoreException
	{
		final Wrapper<Sound> retval = new Wrapper<>();
		Operator.<Sound>perform((dao) -> {
			retval.item = dao.queryForId((int)id);
		}, Sound.class);
		return(retval.item);
	}

	@Override
	public List<Sound> onRead(final Pagination pg,
			final Credentials creds, final Sound pattern)
			throws DatastoreException
	{
		final Wrapper<List<Sound>> retval = new Wrapper<>();
		Operator.<Sound>perform((dao) -> {
			QueryBuilder<Sound, Integer> qb = dao.queryBuilder()
				.setCountOf(pg.returnCount())
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			retval.item = new ArrayList<Sound>();
			filterOnMode(qb.where(), creds);
			retval.count = pg.returnCount() ? qb.countOf() : -1;
			retval.item.addAll((pattern == null)
				? qb.query()
				: dao.queryForMatchingArgs(pattern));
		}, Sound.class);
		pg.setCount(retval.count);
		return(retval.item);
	}

	@Override
	public List<Sound> onRead(final Tag tag, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		final List<Sound> retval = new ArrayList<>();
		Operator.<SoundTags>perform((dao) -> {
			QueryBuilder<SoundTags, Integer> qb = dao.queryBuilder()
				.setCountOf(pg.returnCount())
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			qb.where().eq(SoundTags.COL_TAG, tag);

			for (SoundTags st : qb.query()) {
				retval.add(st.getSound());
			}
			pg.setCount(pg.returnCount() ? qb.countOf() : -1);
		}, SoundTags.class);
		return(retval);
	}

	@Override
	public Sound onUpdate(final Sound sound)
			throws DatastoreException
	{
		Operator.<Sound>perform((dao) -> dao.update(sound),Sound.class);
		return(sound);
	}

	@Override
	public Sound onDelete(final Sound sound)
			throws DatastoreException
	{
		Operator.<Sound>perform((dao) -> dao.delete(sound),Sound.class);
		return(sound);
	}

	@Override
	public Sound onAdd2Tag(final Sound sound, final Tag tag)
			throws DatastoreException
	{
		Operator.<SoundTags>perform((dao) -> {
			dao.createOrUpdate(new SoundTags(sound, tag));
		}, SoundTags.class);
		return(sound);
	}

	@Override
	public Sound onDeleteFromTags(final Sound sound, final Tag tag)
			throws DatastoreException
	{
		Operator.<SoundTags>perform((dao) -> {
			dao.delete(new SoundTags(sound, tag));
		}, SoundTags.class);
		return(sound);
	}

	@Override
	public List<Tag> onGetTags(final Sound sound, final Pagination pg)
			throws DatastoreException
	{
		final List<Tag> retval = new ArrayList<>();
		Operator.<SoundTags>perform((dao) -> {
			QueryBuilder<SoundTags, Integer> qb = dao.queryBuilder()
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			qb.where().eq(SoundTags.COL_SOUND, sound);

			for (SoundTags st : qb.query()) {
				retval.add(st.getTag());
			}
		}, SoundTags.class);
		return(retval);
	}
}
