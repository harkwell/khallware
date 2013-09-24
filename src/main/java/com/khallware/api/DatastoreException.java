// Copyright Kevin D.Hall 2014-2015

package com.khallware.api;

public class DatastoreException extends Exception
{
	private static final long serialVersionUID = 0x0001L;

	public DatastoreException() {}

	public DatastoreException(String message)
	{
		super(message);
	}

	public DatastoreException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public DatastoreException(Throwable cause)
	{
		super(cause);
	}
}
