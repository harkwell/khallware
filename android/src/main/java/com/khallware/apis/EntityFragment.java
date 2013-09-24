// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import com.khallware.apis.enums.EntityType;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.os.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityFragment extends Fragment
{
	private static final Logger logger = LoggerFactory.getLogger(
		EntityFragment.class);
	public static final String ARG_JSON = "json";
	public static final String ARG_ENTITY = "entity";

	// All subclasses of Fragment must include a public empty constructor.
	public EntityFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle)
	{
		View retval = null;
		try {
			EntityType type = null;
			String json = null;
			Bundle args = (getArguments() != null)
				? getArguments()
				: bundle;

			if (args == null) {
				retval = ViewFactory.make(
					new IllegalArgumentException(
						"no argument data passed"),
					getActivity());
			}
			else {
				json = args.getString(ARG_JSON);
				type = EntityType.valueOf(
					args.getString(ARG_ENTITY));
				retval = ViewFactory.make(type, json,
					getActivity());
			}
		}
		catch (Exception e) {
			retval = ViewFactory.make(e, getActivity());
			logger.error(""+e, e);
		}
		return(retval);
	}
}
