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

public class LoadFailureFragment extends Fragment
{
	private static final Logger logger = LoggerFactory.getLogger(
		LoadFailureFragment.class);

	public interface Callback
	{
		public void handle();
	}

	private Callback callback = null;

	// All subclasses of Fragment must include a public empty constructor.
	public LoadFailureFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle)
	{
		View retval = null;
		View button = null;
		try {
			retval = inflater.inflate(R.layout.reload, null, false);
			button = retval.findViewById(R.id.reload_btn);
			button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v)
				{
					if (getCallback() != null) {
						getCallback().handle();
					}
				}
			});
		}
		catch (Exception e) {
			retval = ViewFactory.make(e, getActivity());
			logger.error(""+e, e);
		}
		return(retval);
	}

	public void set(Callback callback)
	{
		this.callback = callback;
	}

	public Callback getCallback()
	{
		return(callback);
	}
}
