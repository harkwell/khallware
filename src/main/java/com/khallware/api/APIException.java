// Copyright Kevin D.Hall 2014-2018

package com.khallware.api;

public class APIException extends Exception
{
	private static final long serialVersionUID = 0x0001L;

	public APIException() {}

	public APIException(String message)
	{
		super(message);
	}

	public APIException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public APIException(Throwable cause)
	{
		super(cause);
	}
}
