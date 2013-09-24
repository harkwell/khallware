// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.validation;

import com.khallware.api.domain.APIEntity;
import java.util.Arrays;
import java.util.List;

/**
 * This validator enforces that specific fields exist in an entity and that they
 * are non empty.
 *
 * @author khall
 */
public class CompleteAPIEntity extends Completeness
{
	private String[] fields = null;

	public CompleteAPIEntity(String... fields)
	{
		this.fields = fields;
	}

	@Override
	public List<String> onGetFieldNames(APIEntity e)
	{
		return(Arrays.<String>asList(fields));
	}
}
