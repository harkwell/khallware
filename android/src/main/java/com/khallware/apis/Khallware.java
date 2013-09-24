// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import android.content.Intent;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;
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

public class Khallware extends Activity
{
	private static final Logger logger = LoggerFactory.getLogger(
		Khallware.class);
	public static final String ARG_TAG = "tag";
	private Datastore dstore = null;

	@Override
	public void onCreate(Bundle bundle)
	{
		int mode = Context.MODE_PRIVATE;
		dstore = Datastore.getDatastore(this, getPreferences(mode));
		super.onCreate(bundle);
		setContentView(R.layout.main);
		// getAboutDialog(this);
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
			toastException(e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// MenuInflater inflater = getMenuInflater();
		// inflater.inflate(R.menu.boxes_menu, menu);
		return(true);
	}

	public void goContacts(View view)
	{
		logger.trace("goContacts()...");
		lauchIntent(ContactsActivity.class);
	}

	public void goBookmarks(View view)
	{
		logger.trace("goBookmarks()...");
		lauchIntent(BookmarksActivity.class);
	}

	public void goBlogs(View view)
	{
		logger.trace("goBlogs()...");
		lauchIntent(BlogsActivity.class);
	}

	public void goEvents(View view)
	{
		logger.trace("goEvents()...");
		lauchIntent(EventsActivity.class);
	}

	public void goFileitems(View view)
	{
		logger.trace("goFileitems()...");
		lauchIntent(FileitemsActivity.class);
	}

	public void goLocations(View view)
	{
		logger.trace("goLocations()...");
		lauchIntent(LocationsActivity.class);
	}

	public void goPhotos(View view)
	{
		logger.trace("goPhotos()...");
		lauchIntent(PhotosActivity.class);
	}

	public void goSearch(View view)
	{
		logger.trace("goSearch()...");
		lauchIntent(SearchActivity.class);
	}

	public void goSounds(View view)
	{
		logger.trace("goSounds()...");
		lauchIntent(SoundsActivity.class);
	}

	public void goVideos(View view)
	{
		logger.trace("goVideos()...");
		lauchIntent(VideosActivity.class);
	}

	protected void lauchIntent(Class clazz)
	{
		try {
			Intent intent = new Intent(this, clazz);
			// KDH get tag from db
			intent.putExtra(ARG_TAG, ""+0);
			startActivity(intent);
		}
		catch (Exception e) {
			toastException(e);
		}
	}

	protected void toastException(Exception e)
	{
		int duration = Toast.LENGTH_SHORT;
		Context context = getApplicationContext();
		String msg = Util.toStringWithStacktrace(e);
		Toast toast = Toast.makeText(context, msg, duration);
		logger.error(""+e, e);
		toast.show();
	}

	/*
	private Dialog getAboutDialog(Context context)
	{
		if (aboutDialog != null) {
			return(aboutDialog);
		}
		Builder builder = new Builder(context);
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = null;
		try {
			view = inflater.inflate(R.layout.about, null, false);
		}
		catch (InflateException e) {
			view = new TextView(context);
			((TextView)view).setText(""+e);
			view.setClickable(false);
		}
		builder.setMessage(R.string.lit_about);
		builder.setView(view);
		aboutDialog = builder.create();
		return(aboutDialog);
	} */
}
