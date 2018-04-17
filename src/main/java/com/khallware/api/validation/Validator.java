// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.validation;

import com.khallware.api.APIException;

/**
 * The Validator concept.  This interfaces defines what a validator is.
 *
 * @author khall
 */
public interface Validator<T>
{
	public void enforce(T criteria) throws APIException;
	public boolean isValid(T criteria);
}
