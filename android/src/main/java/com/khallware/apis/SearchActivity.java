// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import com.khallware.apis.tasks.SimpleTask;
import com.khallware.apis.enums.EntityType;
import android.widget.SimpleAdapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.EditText;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends Activity
{
	private static final Logger logger = LoggerFactory.getLogger(
		SearchActivity.class);

	private List<Map<String,String>> results = new ArrayList<>();

	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.search);
		bundle = (bundle == null) ? getIntent().getExtras() : bundle;
	}

	public void performSearch(View view)
	{
		String token = null;
		EditText editText = (EditText)findViewById(R.id.search_text);
		ListView listView = (ListView)findViewById(R.id.search_results);
		logger.trace("performSearch()...");
		token = (editText == null) ? "" : ""+editText.getText();

		if (!token.isEmpty() && listView != null) {
			update(listView, token);
		}
	}

	public void update(final ListView listView, final String token)
	{
		final Context context = getApplicationContext();
		results.clear();
		new SimpleTask() {
			@Override
			public void perform() throws Exception
			{
				int viewId = R.layout.search_result_row;
				String[] from = new String[] { "name" };
				int[] to = new int[] { R.id.result_name };
				results.addAll(search(token));
				final SimpleAdapter adapter = new SimpleAdapter(
					context, results, viewId, from, to);
				final ListView lv = listView;
				runOnUiThread(new Runnable() {
					public void run()
					{
						lv.setAdapter(adapter);
						lv.setOnItemClickListener(
							onItemClickListener);
						lv.invalidate();
					}
				});
			}
		}.execute();
	}

	public static String sanitize(String input)
	{
		return(input.replaceAll("\\{","").replaceAll("\\}","")
			.replaceAll("\"","").replaceAll("'","")
			.replaceAll("\\[","").replaceAll("\\]","")
			.replaceAll(";","").replaceAll("\\\\",""));
	}

	protected List<Map<String,String>> search(String token)
			throws NetworkException, DatastoreException,
			JSONException
	{
		List<Map<String,String>> retval = new ArrayList<>();
		String[] uup = Datastore.getDatastore().getUrlUserPasswd();
		String url = uup[0]+"/apis/v1/search";
		String key = null;
		JSONArray jarray = null;
		JSONObject rslt = Util.handlePost(url, sanitize(token));

		for (EntityType type : EntityType.values()) {
			if (rslt.has((key = ""+type+"s"))) {
				if ((jarray = rslt.getJSONArray(key)) == null) {
					continue;
				}
				retval.addAll(collate(type, jarray));
			}
		}
		return(retval);
	}

	protected static void overlay(JSONObject jo, Map<String,String> map)
			throws JSONException
	{
		for (Iterator<String> iter = jo.keys(); iter.hasNext();) {
			String key = iter.next();
			map.put(key, jo.getString(key));
		}
	}

	protected static void overlay(Map<String,String> map, JSONObject jo)
			throws JSONException
	{
		String id = map.get("id").split(":")[1];

		for (String key : map.keySet()) {
			jo.put(key, map.get(key));
		}
		jo.put("id", id);
	}

	protected List<Map<String,String>> collate(EntityType type,
			JSONArray jsonArray) throws JSONException
	{
		List<Map<String,String>> retval = new ArrayList<>();
		Map<String,String> map = null;
		JSONObject jsonObj = null;

		for (int idx=0; idx < jsonArray.length(); idx++) {
			if (jsonArray.isNull(idx)) {
				continue;
			}
			jsonObj = jsonArray.getJSONObject(idx);
			overlay(jsonObj, (map = new HashMap<String,String>()));

			if (!map.containsKey("id")) {
				continue;
			}
			map.put("id", ""+type+":"+map.get("id"));

			if (!map.containsKey("name")) {
				map.put("name", map.get("id"));
			}
			retval.add(map);
		}
		return(retval);
	}

	private AdapterView.OnItemClickListener onItemClickListener =
			new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int idx, long identifier)
		{
			Context context = getApplicationContext();
			try {
				Map<String,String> map = results.get(idx);
				String[] dat = map.get("id").split(":");
				EntityType type = EntityType.valueOf(dat[0]);
				int id = Integer.parseInt(dat[1]);
				Intent intent = null;

				switch (type) {
				case tag:
					intent = new Intent(context,
						Khallware.class);
					Datastore.getDatastore().setTag(id);
					break;
				default:
					JSONObject jo = new JSONObject("{}");
					overlay(map, jo);
					intent = new Intent(context,
						CrudActivity.class);
					intent.putExtra(CrudActivity.ARG_JSON,
						""+jo);
					intent.putExtra(CrudActivity.ARG_TYPE,
						""+type);
					intent.putExtra(Khallware.ARG_TAG,
						map.containsKey("tag")
							? map.get("tag")
							: ""+-1);
					break;
				}
				startActivity(intent);
			}
			catch (Exception e) {
				Util.toastException(e, context);
			}
		}
	};
}
