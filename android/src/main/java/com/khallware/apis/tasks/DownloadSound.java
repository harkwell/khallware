// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis.tasks;

import com.khallware.apis.DatastoreException;
import com.khallware.apis.NetworkException;
import com.khallware.apis.Datastore;
import com.khallware.apis.Util;
import android.widget.ImageView;
import android.os.AsyncTask;
import java.util.Map;
import java.net.URL;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DownloadSound extends AsyncTask<Integer, Void, File>
{
	private static final Logger logger = LoggerFactory.getLogger(
		DownloadSound.class);

	public interface Callback
	{
		public void handle(File file);
	}

	private Callback callback = null;
	private File cacheDir = null;

	public DownloadSound(Callback callback, File cacheDir)
	{
		this.callback = callback;
		this.cacheDir = cacheDir;
	}

	@Override
	protected File doInBackground(Integer... soundIds)
	{
		File retval = null;
		try {
			retval = getSound(soundIds[0], cacheDir);
		}
		catch (Exception e) {
			logger.error(""+e, e);
		}
		return(retval);
	}

	@Override
	protected void onPostExecute(File file)
	{
		if (callback != null) {
			callback.handle(file);
		}
	}

	private File getSound(int soundId, File dir) throws DatastoreException,
			NetworkException, IOException
	{
		File retval = null;
		InputStream is = null;
		String name = ""+soundId+".ogg";

		if (!(retval = new File(dir, name)).exists()) {
			Datastore dstore = Datastore.getDatastore();
			String[] uup = dstore.getUrlUserPasswd();
			String url = uup[0]+"/apis/v1/sounds/"+name;
			Map<String, String> map = Util.defaultHeadersAsMap();
			map.put("Accept","application/ogg");

			if (!Util.toFile(retval, (is = Util.queryRESTasStream(
					new HttpGet(url), map, null)))) {
				is.close();
				throw new NetworkException(
					"failed to save sound file: "
					+"\""+retval+"\"");
			}
			is.close();
		}
		return(retval);
	}
}
