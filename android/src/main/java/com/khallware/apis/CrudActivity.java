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
			this.put("name", R.id.tag_name);
			// this.put("mask", R.id.tag_mask);
		}});
	};

	private int tag = 0;
	private EntityType type = null;
	private JSONObject jsonObj = null;

	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		bundle = (bundle == null) ? getIntent().getExtras() : bundle;
		tag = Util.resolveTag(bundle);
		try {
			Context context = getApplicationContext();
			LinearLayout layout = null;
			jsonObj = new JSONObject(""+bundle.get(ARG_JSON));

			switch ((type = EntityType.valueOf(
					""+bundle.get(ARG_TYPE)))) {
			case tag:
				setContentView((layout = (LinearLayout)
					ViewFactory.make(
						R.layout.tag, context)));
				jsonObj.put("parent", tag);
				appendCrudButtons(layout);
				turnOnEditTexts(layout);
				break;
			default:
				String msg = "unhandled type \""+type+"\"";
				Util.toastException(
					new IllegalArgumentException(msg),
					context);
			}
			populateLayout(layout, map.get(type), jsonObj);
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
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
						update(id,map.get(type),layout);
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
		update(-1, map, layout);
	}

	protected void update(int id, Map<String, Integer> map,
			LinearLayout layout) throws JSONException,
			DatastoreException, NetworkException
	{
		JSONObject jsonObj = new JSONObject("{}");

		for (String key : map.keySet()) {
			EditText et = (EditText)layout.findViewById(
				map.get(key));
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
			editText.setEnabled(true);
			editText.setLayoutParams(
				new LinearLayout.LayoutParams(200, 75));
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
