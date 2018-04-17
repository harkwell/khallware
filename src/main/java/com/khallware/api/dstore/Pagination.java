// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.dstore;

/**
 * The Pagination.  This class is used to parcel responses for entities into
 * discrete pages.
 *
 * @author khall
 */
public class Pagination
{
	public static final long DEF_PAGE = 1L;
	public static final long DEF_PAGESIZE = 25L;
	public static final boolean DEF_SORTASC = true;

	private String sortColumn = null;
	private boolean returnCount = false;
	private boolean sortAscending = DEF_SORTASC;
	private long pageSize = DEF_PAGESIZE;
	private long page = DEF_PAGE;
	private long count = 0;

	public Pagination() {}

	public Pagination(long page, long pageSize, boolean returnCount)
	{
		this.page = page;
		this.pageSize = pageSize;
		this.returnCount = returnCount;
	}

	public long calcCursorIndex()
	{
		long retval = ((getPage() - 1) * getPageSize());
		return(Math.max(0, retval));
	}

	public Pagination setPage(long page)
	{
		this.page = page;
		return(this);
	}

	public Pagination prev()
	{
		page--;
		return(this);
	}

	public Pagination next()
	{
		page++;
		return(this);
	}

	public long getPage()
	{
		return(page);
	}

	public Pagination setCount(long count)
	{
		this.count = count;
		return(this);
	}

	public long getCount()
	{
		return(count);
	}

	public Pagination setPageSize(long pageSize)
	{
		this.pageSize = pageSize;
		return(this);
	}

	public long getPageSize()
	{
		return(Math.max(0, pageSize));
	}

	public Pagination setSortBy(String sortColumn)
	{
		this.sortColumn = sortColumn;
		return(this);
	}

	public boolean returnCount()
	{
		return(returnCount);
	}

	public boolean isSorted()
	{
		return(getSortColumn() != null
			&& !getSortColumn().isEmpty());
	}

	public Pagination setSortAscending(boolean value)
	{
		sortAscending = value;
		return(this);
	}

	public boolean isSortAscending()
	{
		return(sortAscending);
	}

	public String getSortColumn()
	{
		return(sortColumn);
	}
}
