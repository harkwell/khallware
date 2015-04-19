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
import java.io.InputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DownloadBitmap extends AsyncTask<Integer, Void, Bitmap>
{
	private static final Logger logger = LoggerFactory.getLogger(
		DownloadBitmap.class);

	private final WeakReference<ImageView> ref;

	public DownloadBitmap(ImageView view)
	{
		ref = new WeakReference<ImageView>(view);
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

	private Bitmap getBitmap(int photoId) throws DatastoreException,
			NetworkException, IOException
	{
		Bitmap retval = null;
		InputStream is = null;
		String[] uup = Datastore.getDatastore().getUrlUserPasswd();
		String url = uup[0]+"/apis/v1/photos/"+photoId+".jpg";
		Map<String, String> map = Util.defaultHeadersAsMap();
		map.put("Accept","image/jpeg");
		is = Util.queryRESTasStream(new HttpGet(url), map);
		is.mark(1024);
		retval = BitmapFactory.decodeStream(is);
		is.reset();

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
