// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis.tasks;

import android.os.AsyncTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleTask extends AsyncTask<Object, Void, Void>
{
	private static final Logger logger = LoggerFactory.getLogger(
		DownloadSound.class);

	public SimpleTask() {}

	@Override
	protected Void doInBackground(Object... nothing)
	{
		try {
			perform();
		}
		catch (Exception e) {
			logger.error(""+e, e);
		}
		return((Void)null);
	}

	public void perform() throws Exception
	{
		throw new IllegalStateException("override this method...");
	}
}
