// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.dstore;

import com.khallware.api.DatastoreException;
import com.khallware.api.Datastore.Wrapper;
import com.khallware.api.domain.APIEntity;
import com.khallware.api.domain.Tag;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * The OrphanUtil.  This class is used by the datastore to append orphaned
 * entities to lists.  It is needed to reorient the pagination datastructure.
 *
 * @author khall
 */
public abstract class OrphanUtil<T extends APIEntity>
{
	private static final Logger logger = LoggerFactory.getLogger(
		OrphanUtil.class);

	public abstract List<T> execGet(Pagination pg)
		throws DatastoreException;

	public List<T> getOrphans(Tag tag, Pagination pg)
	{
		return((tag.getId() == Tag.ROOT)
			? getOrphans(pg)
			: (List<T>)new ArrayList<APIEntity>());
	}

	public List<T> getOrphans(Pagination pg)
	{
		List<T> retval = null;
		final Wrapper<Integer> count = new Wrapper<>();
		Pagination pg1 = new Pagination(1, 25, true) {
			@Override
			public long calcCursorIndex()
			{
				long rslt = super.calcCursorIndex();
				return(Math.max(0,(rslt - count.item)));
			}
		};
		try {
			count.item = new Integer(0);
			pg1.setPageSize(pg.getPageSize());
			retval = execGet(pg1);
			pg.setCount(pg1.getCount() + pg.getCount());
		}
		catch (DatastoreException e) {
			logger.error(""+e, e);
		}
		return(retval);
	}
}
