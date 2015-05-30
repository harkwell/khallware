// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis.tasks;

import com.khallware.apis.DatastoreException;
import com.khallware.apis.NetworkException;
import com.khallware.apis.Datastore;
import com.khallware.apis.Util;
import android.widget.ImageView;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import java.util.Map;
import java.net.URL;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DownloadBitmap extends AsyncTask<Integer, Void, Bitmap>
{
	private static final Logger logger = LoggerFactory.getLogger(
		DownloadBitmap.class);

	private final WeakReference<ImageView> ref;
	private File cacheDir = null;

	public DownloadBitmap(ImageView view, File cacheDir)
	{
		ref = new WeakReference<ImageView>(view);
		this.cacheDir = cacheDir;
	}

	@Override
	protected Bitmap doInBackground(Integer... photoIds)
	{
		Bitmap retval = null;
		try {
			retval = getBitmap(photoIds[0]);
		}
		catch (Exception e) {
			logger.error(""+e, e);
		}
		return(retval);
	}

	@Override
	protected void onPostExecute(Bitmap bitmap)
	{
		if (ref != null && bitmap != null) {
			final ImageView imageView = ref.get();

			if (imageView != null) {
				imageView.setImageBitmap(bitmap);
			}
		}
	}

	private Bitmap getBitmapFromCache(File dir, int photoId)
	{
		Bitmap retval = null;
		try {
			File file = new File(dir, ""+photoId+".jpg");

			if (dir != null && file.exists()) {
				retval = BitmapFactory.decodeStream(
					new FileInputStream(file));
			}
		}
		catch (Exception e) {
			logger.error(""+e, e);
		}
		return(retval);
	}

	private Bitmap getBitmap(int photoId) throws DatastoreException,
			NetworkException, IOException
	{
		Bitmap retval = getBitmapFromCache(cacheDir, photoId);
		InputStream is = null;
		String[] uup = Datastore.getDatastore().getUrlUserPasswd();
		String name = photoId+".jpg";
		String url = uup[0]+"/apis/v1/photos/"+name+"?thumb=true";
		Map<String, String> map = Util.defaultHeadersAsMap();
		map.put("Accept","image/jpeg");
		is = Util.queryRESTasStream(new HttpGet(url), map, null);
		is.mark(1024);
		retval = BitmapFactory.decodeStream(is);
		is.reset();

		if (cacheDir != null) {
			Util.toFile(new File(cacheDir, name), is);
			is.reset();
		}
		if (retval == null) {
			logger.info(Util.toString(is).substring(0, 80));
			is.reset();
			url = uup[0]+"/apis/media/qrcode.png";
			retval = BitmapFactory.decodeStream(
				new URL(url).openConnection().getInputStream());
		}
		return(retval);
	}
}
