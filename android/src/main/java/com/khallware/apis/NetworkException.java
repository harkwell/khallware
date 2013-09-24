// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

public class NetworkException extends Exception
{
	private static final long serialVersionUID = 0x0001L;

	public NetworkException()
	{
	}

	public NetworkException(String message)
	{
		super(message);
	}

	public NetworkException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public NetworkException(Throwable cause)
	{
		super(cause);
	}
}
