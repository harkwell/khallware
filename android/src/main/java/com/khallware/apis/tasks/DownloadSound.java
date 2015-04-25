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
import java.io.FileOutputStream;
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
		File retval = new File(dir, ""+soundId+".ogg");

		if (!retval.exists()) {
			Datastore dstore = Datastore.getDatastore();
			FileOutputStream fos = new FileOutputStream(retval);
			String[] uup = dstore.getUrlUserPasswd();
			String url = uup[0]+"/apis/v1/sounds/"+soundId+".ogg";
			Map<String, String> map = Util.defaultHeadersAsMap();
			InputStream is = null;
			map.put("Accept","application/ogg");
			is = Util.queryRESTasStream(new HttpGet(url), map);

			for (int ch=-1; (ch = is.read()) != -1;) {
				fos.write(ch);
			}
			is.close();
			fos.close();
		}
		return(retval);
	}
}
