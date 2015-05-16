// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import com.khallware.apis.enums.EntityType;
import android.provider.MediaStore.Images;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Dialog;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.SurfaceView;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

public class Khallware extends Activity
{
	private static final Logger logger = LoggerFactory.getLogger(
		Khallware.class);
	public static final String ARG_TAG = "tag";
	public static final int ACTIVITY_SELECT_IMAGE = 1;
	private Dialog aboutDialog = null;
	private Datastore dstore = null;

	@Override
	public void onCreate(Bundle bundle)
	{
		int mode = Context.MODE_PRIVATE;
		dstore = Datastore.getDatastore(this, getPreferences(mode));
		super.onCreate(bundle);
		setContentView(R.layout.main);
		try {
			final int id = dstore.getTag();
			((EditText)findViewById(R.id.atag_id)).setText(""+id);
			AsyncTask.execute(new Runnable() {
				public void run()
				{
					try {
						applyTagInfo(
							CrudHelper.read(
								EntityType.tag,
								id));
						countAndApply(id);
					}
					catch (NetworkException
							|DatastoreException e) {
						logger.error(""+e,e);
					}
				}
			});
		}
		catch (DatastoreException e) {
			Util.toastException(e, getApplicationContext());
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();
		try {
			String[] uup = dstore.getUrlUserPasswd();

			if (uup[0].isEmpty()) {
				Intent intent = new Intent(this,
					ConnectActivity.class);
				startActivity(intent);
			}
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return(true);
	}

	public void addTag(View view)
	{
		logger.trace("addTag()...");
		try {
			Map<String, String> map = new HashMap<>();
			map.put(ARG_TAG, ""+dstore.getTag());
			map.put(CrudActivity.ARG_JSON, "{}");
			map.put(CrudActivity.ARG_TYPE, ""+EntityType.tag);
			launchIntent(CrudActivity.class, map);
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
		}
	}

	public void goTags(View view)
	{
		logger.trace("goTags()...");
		launchIntent(TagsActivity.class);
	}

	public void goContacts(View view)
	{
		logger.trace("goContacts()...");
		launchIntent(ContactsActivity.class);
	}

	public void postContacts(View view)
	{
		Context context = getApplicationContext();
		logger.trace("postContacts()...");
		try {
			Util.postContacts(context, dstore.getTag());
		}
		catch (Exception e) {
			Util.toastException(e, context);
		}
	}

	public void goBookmarks(View view)
	{
		logger.trace("goBookmarks()...");
		launchIntent(BookmarksActivity.class);
	}

	public void goBlogs(View view)
	{
		logger.trace("goBlogs()...");
		launchIntent(BlogsActivity.class);
	}

	public void goEvents(View view)
	{
		logger.trace("goEvents()...");
		launchIntent(EventsActivity.class);
	}

	public void goFileitems(View view)
	{
		logger.trace("goFileitems()...");
		launchIntent(FileitemsActivity.class);
	}

	public void goLocations(View view)
	{
		logger.trace("goLocations()...");
		launchIntent(LocationsActivity.class);
	}

	public void goPhotos(View view)
	{
		logger.trace("goPhotos()...");
		launchIntent(PhotosActivity.class);
	}

	public void uploadPhotos(View view)
	{
		logger.trace("uploadPhotos()...");
		try {
			Intent intent = new Intent(Intent.ACTION_PICK,
				Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(intent, ACTIVITY_SELECT_IMAGE);
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
		}
	}

	public void goSearch(View view)
	{
		logger.trace("goSearch()...");
		launchIntent(SearchActivity.class);
	}

	public void goSounds(View view)
	{
		logger.trace("goSounds()...");
		launchIntent(SoundsActivity.class);
	}

	public void goVideos(View view)
	{
		logger.trace("goVideos()...");
		launchIntent(VideosActivity.class);
	}

	public void goConnect(View view)
	{
		try {
			logger.trace("goConnect()...");
			dstore.deleteUrlUserPasswd();
			launchIntent(Khallware.class);
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
		}
	}

	protected void onActivityResult(int request, int result, Intent intent)
	{
		super.onActivityResult(request, result, intent);

		switch(request) {
		case ACTIVITY_SELECT_IMAGE:
			/*
			String[] cols = { Images.Media.DATA };
			Cursor cursor = getContentResolver().query(
				intent.getData(), cols, null, null, null);
			cursor.moveToFirst();
			final File file = new File(cursor.getString(
				cursor.getColumnIndex(cols[0])));
			cursor.close();
			AsyncTask.execute(new Runnable() {
				public void run()
				{
					try {
						CrudHelper.upload(
							EntityType.photo,
							dstore.getTag(), file);
					}
					catch (Exception e) {
						logger.error(""+e,e);
					}
				}
			});
			*/
			break;
		}
	}

	protected void launchIntent(Class clazz)
	{
		Map<String, String> map = new HashMap<>();
		try {
			map.put(ARG_TAG, ""+dstore.getTag());
			launchIntent(clazz, map);
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
		}
	}

	protected void launchIntent(Class clazz, Map<String, String> map)
	{
		try {
			Intent intent = new Intent(this, clazz);

			for (String key : map.keySet()) {
				intent.putExtra(key, map.get(key));
			}
			startActivity(intent);
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
		}
	}

	protected void applyTagInfo(final JSONObject tag)
	{
		runOnUiThread(new Runnable() {
			public void run()
			{
				try {
					((EditText)findViewById(
						R.id.atag_name)).setText(
							tag.getString("name"));
					((EditText)findViewById(
						R.id.atag_desc)).setText(
							tag.getString(
								"description"));
				}
				catch (Exception e) {
					logger.error(""+e,e);
				}
			}
		});
	}

	protected void applyButtonInfo(final int viewId, final long count)
	{
		runOnUiThread(new Runnable() {
			public void run()
			{
				Button button = null;
				String label = null;
				int idx = -1;
				try {
					button = (Button)findViewById(viewId);
					label = (button != null)
						? ""+button.getText()
						: "";
					idx = label.indexOf(" (");
					idx = (idx > 0) ? idx : label.length();
					label = label.substring(0, idx);

					if (button != null) {
						button.setText(
							label+" ("+count+")");
					}
				}
				catch (Exception e) {
					logger.error(""+e,e);
				}
			}
		});
	}

	protected void countAndApply(int tagId)
	{
		Map<EntityType, Integer> map = new HashMap<>();
		map.put(EntityType.photo, R.id.aphoto_button);

		try {
			for (EntityType type : map.keySet()) {
				long count = CrudHelper.count(type, tagId);
				applyButtonInfo(map.get(type), count);
			}
		}
		catch (NetworkException|DatastoreException e) {
			logger.error(""+e, e);
		}
	}
}
