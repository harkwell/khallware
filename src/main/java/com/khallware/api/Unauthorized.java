// Copyright Kevin D.Hall 2014-2018

package com.khallware.api;

public class Unauthorized extends APIException
{
	private static final long serialVersionUID = 0x0001L;

	public Unauthorized() {}

	public Unauthorized(String message)
	{
		super(message);
	}

	public Unauthorized(String message, Throwable cause)
	{
		super(message, cause);
	}

	public Unauthorized(Throwable cause)
	{
		super(cause);
	}
}
