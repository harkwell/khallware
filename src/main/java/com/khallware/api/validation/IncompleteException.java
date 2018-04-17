// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.validation;

import com.khallware.api.APIException;

public class IncompleteException extends APIException
{
	private static final long serialVersionUID = 0x0001L;

	public IncompleteException() {}

	public IncompleteException(String message)
	{
		super(message);
	}

	public IncompleteException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public IncompleteException(Throwable cause)
	{
		super(cause);
	}
}
