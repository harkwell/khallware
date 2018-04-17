// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.validation;

import com.khallware.api.APIException;

public class PolicyViolation extends APIException
{
	private static final long serialVersionUID = 0x0001L;

	public PolicyViolation() {}

	public PolicyViolation(String message)
	{
		super(message);
	}

	public PolicyViolation(String message, Throwable cause)
	{
		super(message, cause);
	}

	public PolicyViolation(Throwable cause)
	{
		super(cause);
	}
}
