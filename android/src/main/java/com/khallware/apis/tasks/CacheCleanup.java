// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis.tasks;

import android.os.AsyncTask;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheCleanup extends AsyncTask<File, Void, Void>
{
	private static final Logger logger = LoggerFactory.getLogger(
		CacheCleanup.class);

	private static final FilenameFilter filter = new FilenameFilter() {
		public boolean accept(File dir, String name)
		{
			boolean retval = false;
			String[] extensions = new String[] {
				".ogg", ".jpg", ".mp4"
			};
			for (String extension : extensions) {
				if (retval |= name.endsWith(extension)) {
					break;
				}
			}
			return(retval);
		}
	};

	public CacheCleanup() {}

	@Override
	protected Void doInBackground(File... files)
	{
		try {
			delete(files);
		}
		catch (IOException e) {
			logger.error(""+e, e);
		}
		return((Void)null);
	}

	private static void delete(File[] files) throws IOException
	{
		for (File file : files) {
			delete(file);
		}
	}

	private static void delete(File file) throws IOException
	{
		if (file.isFile()) {
			file.delete();
		}
		else {
			delete(file.listFiles(filter));
		}
	}
}
