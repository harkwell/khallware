// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import com.khallware.apis.enums.EntityType;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.Button;
import android.app.Activity;
import android.os.AsyncTask;
import android.content.Context;
import android.view.View;
import android.os.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class CrudActivity extends Activity
{
	public static final String ARG_JSON = "json";
	public static final String ARG_TYPE = "type";
	private static final Logger logger = LoggerFactory.getLogger(
		CrudActivity.class);
	private static final Map<EntityType, Map<String, Integer>> map =
		new HashMap<>();

	static {
		map.put(EntityType.tag, new HashMap<String, Integer>() {{
			this.put("name",    R.id.tag_name);
			this.put("id",      R.id.tag_id);
			this.put("parent",  R.id.tag_parent);
			// this.put("mask", R.id.tag_mask);
		}});
		map.put(EntityType.photo, new HashMap<String, Integer>() {{
			this.put("name",    R.id.photo_name);
			this.put("desc",    R.id.photo_desc);
			// this.put("mask", R.id.tag_mask);
		}});
		map.put(EntityType.bookmark, new HashMap<String, Integer>() {{
			this.put("name",    R.id.bookmark_name);
			this.put("desc",    R.id.bookmark_desc);
			this.put("rating",  R.id.bookmark_rating);
			this.put("url",     R.id.bookmark_url);
			this.put("title",   R.id.bookmark_title);
			// this.put("mask", R.id.tag_mask);
		}});
		map.put(EntityType.contact, new HashMap<String, Integer>() {{
			this.put("name",    R.id.contact_name);
			this.put("email",   R.id.contact_email);
			this.put("phone",   R.id.contact_phone);
			this.put("title",   R.id.contact_title);
			this.put("address", R.id.contact_address);
			this.put("desc",    R.id.contact_desc);
			this.put("org",     R.id.contact_org);
		}});
		map.put(EntityType.event, new HashMap<String, Integer>() {{
			this.put("name",    R.id.event_name);
			this.put("desc",    R.id.event_desc);
			this.put("duration",R.id.event_duration);
			this.put("start",   R.id.event_start);
			this.put("end",     R.id.event_end);
		}});
		map.put(EntityType.location, new HashMap<String, Integer>() {{
			this.put("name",     R.id.location_name);
			this.put("latitude", R.id.location_latitude);
			this.put("longitude",R.id.location_longitude);
			this.put("address",  R.id.location_address);
			this.put("title",    R.id.location_title);
			this.put("mask",     R.id.location_mask);
			this.put("desc",     R.id.location_desc);
		}});
	};

	private int tag = -1;
	private EntityType type = null;
	private JSONObject jsonObj = null;

	@Override
	public void onCreate(Bundle bundle)
	{
		Context context = null;
		super.onCreate(bundle);
		bundle = (bundle == null) ? getIntent().getExtras() : bundle;
		try {
			LinearLayout layout = null;
			tag = Util.resolveTag(bundle);
			context = getApplicationContext();
			jsonObj = new JSONObject(""+bundle.get(ARG_JSON));

			switch ((type = EntityType.valueOf(
					""+bundle.get(ARG_TYPE)))) {
			case tag:
				setContentView((layout = (LinearLayout)
					ViewFactory.make(
						R.layout.tag, context)));
				break;
			case photo:
				setContentView((layout = (LinearLayout)
					ViewFactory.make(
						R.layout.photo, context)));
				break;
			case bookmark:
				setContentView((layout = (LinearLayout)
					ViewFactory.make(
						R.layout.bookmark, context)));
				break;
			case contact:
				setContentView((layout = (LinearLayout)
					ViewFactory.make(
						R.layout.contact, context)));
				break;
			case event:
				setContentView((layout = (LinearLayout)
					ViewFactory.make(
						R.layout.event, context)));
				break;
			case location:
				setContentView((layout = (LinearLayout)
					ViewFactory.make(
						R.layout.location, context)));
				break;
			default:
				String msg = "unhandled type \""+type+"\"";
				setContentView((layout = (LinearLayout)
					ViewFactory.make(msg, context)));
				Util.toastException(
					new IllegalArgumentException(msg),
					context);
			}
			appendCrudButtons(layout);
			turnOnEditTexts(layout);
			populateLayout(layout, map.get(type), jsonObj);
			layout.invalidate();
		}
		catch (Exception e) {
			Util.toastException(e, context);
			setContentView(ViewFactory.make(e, context));
		}
	}

	public void update(View view)
	{
		final LinearLayout layout = (LinearLayout)view.getParent();
		final JSONObject j = this.jsonObj;
		logger.trace("update()...");
		AsyncTask.execute(new Runnable() {
			public void run()
			{
				try {
					if (jsonObj.has("id")) {
						int id = Integer.parseInt(
							j.getString("id"));
						update(id, map.get(type));
					}
					else {
						create(map.get(type), layout);
					}
				}
				catch (Exception e) {
					logger.error(""+e, e);
				}
			}
		});
	}

	public void delete(View view)
	{
		final int p3 = tag;
		final EntityType p1 = type;
		final JSONObject p2 = jsonObj;
		logger.trace("delete()...");
		AsyncTask.execute(new Runnable() {
			public void run()
			{
				try {
					int id = Integer.parseInt(
						p2.getString("id"));
					CrudHelper.delete(p1, id, p3);
				}
				catch (Exception e) {
					logger.error(""+e, e);
				}
			}
		});
		finish();
	}

	protected void create(Map<String, Integer> map, LinearLayout layout)
			throws JSONException, DatastoreException,
			NetworkException
	{
		update(-1, map); // KDH handle case where permission denied
	}

	protected void update(int id, Map<String, Integer> map)
			throws JSONException, DatastoreException,
			NetworkException
	{
		JSONObject jsonObj = new JSONObject("{}");

		for (String key : map.keySet()) {
			EditText et = (EditText)findViewById(map.get(key));
			jsonObj.put(key, (et != null) ? ""+et.getText() : "");
		}
		if (id > 0) {
			jsonObj.put("id", id);
			CrudHelper.update(type, jsonObj, tag);
		}
		else {
			CrudHelper.create(type, jsonObj, tag);
		}
		finish();
	}

	protected void populateLayout(LinearLayout layout,
			Map<String, Integer> map, JSONObject jsonObj)
			throws JSONException
	{
		EditText editText = null;

		for (String key : map.keySet()) {
			editText = (EditText)layout.findViewById(map.get(key));

			if (editText != null && jsonObj.has(key)) {
				editText.setText(jsonObj.getString(key));
			}
		}
	}

	protected void turnOnEditTexts(LinearLayout layout)
	{
		EditText editText = null;

		for (int idx=0; idx < layout.getChildCount(); idx++) {
			if (layout.getChildAt(idx) instanceof LinearLayout) {
				turnOnEditTexts((LinearLayout)
					layout.getChildAt(idx));
			}
			if (!(layout.getChildAt(idx) instanceof EditText)) {
				continue;
			}
			editText = (EditText)layout.getChildAt(idx);
			editText.setFocusableInTouchMode(true);
			editText.setFocusable(true);
			editText.setEnabled(true);
			editText.invalidate();
			/* editText.setLayoutParams(
				new LinearLayout.LayoutParams(200, 75)); */
		}
	}

	protected void appendCrudButtons(LinearLayout parent)
	{
		LinearLayout layout = new LinearLayout(parent.getContext());
		Button updateButton = new Button(layout.getContext());
		Button deleteButton = new Button(layout.getContext());
		final boolean hasId = jsonObj.has("id");
		updateButton.setClickable(true);
		deleteButton.setClickable(true);
		updateButton.setText(hasId ? "Update" : "Add");
		deleteButton.setText("Delete");
		updateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				update(v);
			}
		});
		deleteButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				delete(v);
			}
		});
		layout.addView(updateButton);

		if (hasId) {
			layout.addView(deleteButton);
		}
		parent.addView(layout);
	}
}
