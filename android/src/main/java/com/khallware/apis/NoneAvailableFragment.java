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

public class NoneAvailableFragment extends Fragment
{
	private static final Logger logger = LoggerFactory.getLogger(
		NoneAvailableFragment.class);

	// All subclasses of Fragment must include a public empty constructor.
	public NoneAvailableFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle)
	{
		View retval = null;
		try {
			String msg = "No Items Found";
			retval = ViewFactory.make(msg, getActivity());
		}
		catch (Exception e) {
			retval = ViewFactory.make(e, getActivity());
			logger.error(""+e, e);
		}
		return(retval);
	}
}
