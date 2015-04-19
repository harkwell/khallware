// Copyright Kevin D.Hall 2014-2015
/**
 * $Id$
 * ============================================================================
 * ViewFactory.java is the class that instantiates appropriate views
 *
 * The Android "Khallware" application
 * (C) 2014 Kevin D.Hall
 *
 * The software, processes, trade secrets and technical/business know-how used
 * on these premises are the property of Kevin D.Hall and are not to be copied,  * divulged or used without the express written consent of the author.
 */
package com.khallware.apis;

import com.khallware.apis.tasks.DownloadBitmap;
import com.khallware.apis.enums.EntityType;
import android.view.InflateException;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup.LayoutParams;
import android.text.method.ScrollingMovementMethod;
import android.content.Context;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.EditText;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class ViewFactory
{
	private static Logger logger = LoggerFactory.getLogger(
			ViewFactory.class);
	public static final int INIT_SEQ_VAL = 100000;
	public static int sequence = INIT_SEQ_VAL;

	/**
	 * A method to inflate a given view id.
	 */
	public static View make(int viewId, Context context)
	{
		View retval = null;
		LayoutInflater inflater = LayoutInflater.from(context);
		try {
			retval = inflater.inflate(viewId, null, false);
		}
		catch (InflateException e) {
			retval = make(e, context);
		}
		return(retval);
	}

	public static View make(EntityType type, String json, Context context)
	{
		View retval = null;
		JSONObject jsonObj = null;
		try {
			Map<String, Integer> map = new HashMap<>();
			jsonObj = new JSONObject(json);

			switch (type) {
			case tag:
				retval = make(R.layout.tag, context);
				map.put("id",          R.id.tag_id);
				map.put("name",        R.id.tag_name);
				map.put("description", R.id.tag_desc);
				break;
			case bookmark:
				retval = make(R.layout.bookmark, context);
				map.put("name",        R.id.bookmark_name);
				map.put("description", R.id.bookmark_desc);
				map.put("rating",      R.id.bookmark_rating);
				map.put("url",         R.id.bookmark_url);
				map.put("title",       R.id.bookmark_title);
				map.put("updated",     R.id.bookmark_updated);
				break;
			case event:
				retval = make(R.layout.event, context);
				map.put("name",        R.id.event_name);
				map.put("description", R.id.event_desc);
				map.put("duration",    R.id.event_duration);
				map.put("start",       R.id.event_start);
				map.put("end",         R.id.event_end);
				break;
			case contact:
				retval = make(R.layout.contact, context);
				map.put("name",         R.id.contact_name);
				map.put("email",        R.id.contact_email);
				map.put("phone",        R.id.contact_phone);
				map.put("title",        R.id.contact_title);
				map.put("address",      R.id.contact_address);
				map.put("description",  R.id.contact_desc);
				map.put("organization", R.id.contact_org);
				map.put("updated",      R.id.contact_updated);
				break;
			case location:
				retval = make(R.layout.location, context);
				map.put("name",        R.id.location_name);
				map.put("latitude",    R.id.location_latitude);
				map.put("longitude",   R.id.location_longitude);
				map.put("address",     R.id.location_address);
				map.put("title",       R.id.location_title);
				map.put("mask",        R.id.location_mask);
				map.put("modified",    R.id.location_modified);
				map.put("description", R.id.location_desc);
				break;
			case sound:
				retval = make(R.layout.sound, context);
				map.put("name",        R.id.sound_name);
				map.put("description", R.id.sound_desc);
				map.put("title",       R.id.sound_title);
				map.put("artist",      R.id.sound_artist);
				map.put("genre",       R.id.sound_genre);
				map.put("recording",   R.id.sound_recording);
				map.put("publisher",   R.id.sound_publisher);
				map.put("updated",     R.id.sound_updated);
				map.put("mask",        R.id.sound_mask);
				map.put("path",        R.id.sound_path);
				break;
			case photo:
				int id = Integer.parseInt(Util.get("id", json));
				retval = make(R.layout.photo, context);
				map.put("name",        R.id.photo_name);
				map.put("description", R.id.photo_desc);
				new DownloadBitmap((ImageView)
					retval.findViewById(R.id.photo)
				).execute(id);
				break;
			}
			for (String key : map.keySet()) {
				String val = jsonObj.has(key)
					? jsonObj.getString(key)
					: "unknown";
				EditText editText = (EditText)
					retval.findViewById(map.get(key));
				editText.setText(val);
			}
		}
		catch (Exception e) {
			retval = make(e, context);
		}
		return(retval);
	}

	/**
	 * Make a simple output view having the given message.
	 */
	public static View make(String message, Context context)
	{
		final TextView retval = new TextView(context);
		retval.setId(sequence++);
		retval.setMovementMethod(new ScrollingMovementMethod());
		retval.setText(message);
		retval.setClickable(false);
		return(retval);
	}

	/**
	 * Make a simple output view displaying the given exception.
	 */
	public static View make(Exception exception, Context context)
	{
		return(make(Util.toStringWithStacktrace(exception), context));
	}

	/**
	 * Provision the View to have the width of its parent container.
	 */
	public static void fillParentWidth(View view)
	{
		LayoutParams parms = getLayoutParams(view);
		parms.width = LayoutParams.FILL_PARENT;
		view.setLayoutParams(parms);
	}

	/**
	 * Provision the View to have the height of its parent container.
	 */
	public static void fillParentHeight(View view)
	{
		LayoutParams parms = getLayoutParams(view);
		parms.height = LayoutParams.FILL_PARENT;
		view.setLayoutParams(parms);
	}

	/**
	 * Return the View's android UI LayoutParams or create them if null.
	 */
	public static LayoutParams getLayoutParams(View view)
	{
		LayoutParams retval = view.getLayoutParams();

		if (retval == null) {
			retval = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		}
		return(retval);
	}
}
