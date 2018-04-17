// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.validation;

import com.khallware.api.APIException;

public class DuplicateException extends APIException
{
	private static final long serialVersionUID = 0x0001L;

	public DuplicateException() {}

	public DuplicateException(String message)
	{
		super(message);
	}

	public DuplicateException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public DuplicateException(Throwable cause)
	{
		super(cause);
	}
}
