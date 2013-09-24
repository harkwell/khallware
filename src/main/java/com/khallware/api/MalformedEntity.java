// Copyright Kevin D.Hall 2014-2015

package com.khallware.api;

public class MalformedEntity extends APIException
{
	private static final long serialVersionUID = 0x0001L;

	public MalformedEntity() {}

	public MalformedEntity(String message)
	{
		super(message);
	}

	public MalformedEntity(String message, Throwable cause)
	{
		super(message, cause);
	}

	public MalformedEntity(Throwable cause)
	{
		super(cause);
	}
}
