// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import com.khallware.apis.enums.EntityType;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ToggleButton;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Dialog;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.provider.MediaStore.Images;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.SurfaceView;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONException;
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
	public static final int ACTIVITY_SELECT_GEOCODE = 2;
	private Dialog aboutDialog = null;
	private Datastore dstore = null;
	private int tagId = -1;

	@Override
	public void onCreate(Bundle bundle)
	{
		int mode = Context.MODE_PRIVATE;
		dstore = Datastore.getDatastore(this, getPreferences(mode));
		super.onCreate(bundle);
		setContentView(R.layout.main);
		try {
			final int id = (tagId = dstore.getTag());
			((EditText)findViewById(R.id.atag_id)).setText(""+id);
			((ToggleButton)findViewById(
				R.id.favorite_button)).setChecked(
					dstore.isFavorite(tagId));
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		boolean retval = false;
		Context context = getApplicationContext();
		try {
			switch (item.getItemId()) {
			case R.id.about:
				showAbout(context);
				retval = true;
				break;
			case R.id.favorites:
				showFavorites(context);
				retval = true;
				break;
			case R.id.connect:
				goConnect(null);
				retval = true;
				break;
			default:
				retval = super.onOptionsItemSelected(item);
				break;
			}
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
		}
		return(retval);
	}

	/** THE FOLLOWING ARE CALLED VIA LAYOUT XML onClick() **/

	public void toggleFavorite(View view)
	{
		logger.trace("toggleFavorite()...");
		try {
			if (dstore.isFavorite(tagId)) {
				dstore.removeFavorite(tagId);
			}
			else {
				final int id = tagId;
				AsyncTask.execute(new Runnable() {
					public void run()
					{
						try {
							dstore.addFavorite(
								id,
								getTagName(id));
						}
						catch (Exception e) {
							logger.error(""+e,e);
						}
					}
				});
			}
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
		}
	}

	protected String getTagName(int tagId) throws DatastoreException,
			NetworkException, JSONException
	{
		String retval = "unknown tag name";
		JSONObject jsonObj = CrudHelper.read(EntityType.tag, tagId);

		if (jsonObj != null && jsonObj.has("name")) {
			retval = jsonObj.getString("name");
		}
		return(retval);
	}

	public void addTag(View view)
	{
		try {
			logger.trace("addTag()...");
			addEntity(tagId, EntityType.tag);
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

	public void addContact(View view)
	{
		try {
			logger.trace("addContact()...");
			addEntity(tagId, EntityType.contact);
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
		}
	}

	public void goContacts(View view)
	{
		logger.trace("goContacts()...");
		launchIntent(ContactsActivity.class);
	}

	public void postContacts(View view)
	{
		final Context context = getApplicationContext();
		logger.trace("postContacts()...");
		AsyncTask.execute(new Runnable() {
			public void run()
			{
				try {
					Util.postContacts(context, tagId);
				}
				catch (Exception e) {
					logger.error(""+e,e);
				}
			}
		});
	}

	public void replaceContacts(View view)
	{
		final Context context = getApplicationContext();
		logger.trace("replaceContacts()...");
		AsyncTask.execute(new Runnable() {
			public void run()
			{
				try {
					Util.replaceContacts(context, tagId);
				}
				catch (Exception e) {
					logger.error(""+e,e);
				}
			}
		});
	}

	public void addBookmark(View view)
	{
		try {
			logger.trace("addBookmark()...");
			addEntity(tagId, EntityType.bookmark);
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
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

	public void addEvent(View view)
	{
		try {
			logger.trace("addEvent()...");
			addEntity(tagId, EntityType.event);
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
		}
	}

	public void postEvents(View view)
	{
		final Context context = getApplicationContext();
		logger.trace("postEvents()...");
		AsyncTask.execute(new Runnable() {
			public void run()
			{
				try {
					Util.postEvents(context, tagId);
				}
				catch (Exception e) {
					logger.error(""+e,e);
				}
			}
		});
	}

	public void goEvents(View view)
	{
		logger.trace("goEvents()...");
		launchIntent(EventsActivity.class);
	}

	public void replaceEvents(View view)
	{
		final Context context = getApplicationContext();
		logger.trace("replaceEvents()...");
		AsyncTask.execute(new Runnable() {
			public void run()
			{
				try {
					Util.replaceEvents(context, tagId);
				}
				catch (Exception e) {
					logger.error(""+e,e);
				}
			}
		});
	}

	public void goFileitems(View view)
	{
		logger.trace("goFileitems()...");
		launchIntent(FileitemsActivity.class);
	}

	public void addLocation(View view)
	{
		logger.trace("addLocation()...");
		try {
			logger.trace("addLocation()...");
			addEntity(tagId, EntityType.location);
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
		}
	}

	public void pickLocation(View view)
	{
		logger.trace("addLocation()...");
		try {
			Intent intent = new Intent(this, KMapActivity.class);
			startActivityForResult(intent, ACTIVITY_SELECT_GEOCODE);
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
		}
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

	protected void showAbout(Context ctxt)
	{
		if (aboutDialog == null) {
			aboutDialog = new Builder(Khallware.this)
				.setTitle(R.string.lit_about)
				.setView(ViewFactory.make(R.layout.about, ctxt))
				.create();
		}
		aboutDialog.show();
	}

	protected void showFavorites(Context context) throws DatastoreException
	{
		LinearLayout layout = (LinearLayout)ViewFactory.make(
			R.layout.favorites, context);
		final Dialog dialog = new Builder(Khallware.this)
			.setTitle(R.string.lit_favorites)
			.setView(layout)
			.create();
		ListView listView = (ListView)
			layout.findViewById(R.id.favorites_listview);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent,
					View view, int position, long id)
			{
				int viewId = R.id.favorite_id;
				dialog.dismiss();
				launchIntent(
					Khallware.class, 
					Integer.parseInt(""+((TextView)
						view.findViewById(viewId)
						).getText()));
			}
		});
		listView.setAdapter(new KResourceCursorAdapter(
		        this, R.layout.favorites_listview_row,
			dstore.getFavoritesCursor(), 0));
		dialog.show();
	}

	protected void onActivityResult(int request, int result, Intent intent)
	{
		super.onActivityResult(request, result, intent);

		if (result != Activity.RESULT_OK) {
			Util.toastException(
				new RuntimeException("sub activity failure"),
				getApplicationContext());
			return;
		}
		switch(request) {
		case ACTIVITY_SELECT_GEOCODE:
			Bundle bundle = intent.getExtras();
			double latitude = (bundle != null)
				? Double.parseDouble(""+bundle.get(
					KMapActivity.ARG_LATITUDE))
				: 0d;
			double longitude = (bundle != null)
				? Double.parseDouble(""+bundle.get(
					KMapActivity.ARG_LONGITUDE))
				: 0d;
			String json = new StringBuilder()
				.append("{\"latitude\":"+latitude)
				.append(",\"longitude\":"+longitude)
				.append(",\"name\":\"\"}")
				.toString();
			Map<String, String> map = new HashMap<>();
			map.put(ARG_TAG, ""+tagId);
			map.put(CrudActivity.ARG_JSON, json);
			map.put(CrudActivity.ARG_TYPE, ""+EntityType.location);
			launchIntent(CrudActivity.class, map);
			break;
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
		launchIntent(clazz, tagId);
	}

	protected void launchIntent(Class clazz, int tagId)
	{
		Map<String, String> map = new HashMap<>();
		try {
			map.put(ARG_TAG, ""+tagId);
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

	protected void addEntity(int tagId, EntityType type)
	{
		try {
			String json = "{}";
			Map<String, String> map = new HashMap<>();

			switch (type) {
			case tag:
				json = "{\"parent\":"+tagId+"}";
				break;
			}
			map.put(ARG_TAG, ""+tagId);
			map.put(CrudActivity.ARG_JSON, json);
			map.put(CrudActivity.ARG_TYPE, ""+type);
			launchIntent(CrudActivity.class, map);
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
		map.put(EntityType.bookmark, R.id.abookmark_button);
		map.put(EntityType.location, R.id.alocation_button);
		map.put(EntityType.fileitem, R.id.afileitem_button);
		map.put(EntityType.contact, R.id.acontact_button);
		map.put(EntityType.video, R.id.avideo_button);
		map.put(EntityType.sound, R.id.asound_button);
		map.put(EntityType.event, R.id.aevent_button);
		map.put(EntityType.photo, R.id.aphoto_button);
		map.put(EntityType.blog, R.id.ablog_button);

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
