// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import com.khallware.apis.enums.EntityType;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class EntitySetPagerAdapter extends FragmentStatePagerAdapter
{
	private static final Logger logger = LoggerFactory.getLogger(
		FragmentStatePagerAdapter.class);
	public static final int MAX_ENTITIES = 1000;
	public static final int MAX_REQ_ENTITIES = 25;

	private final Map<Integer,Fragment> loadMap = new HashMap<>();
	private final List<String> jsonList = new Vector<>();
	private FragmentManager fragmentManager = null;
	private EntityType entityType = null;
	private boolean endOfItems = false;
	private boolean processing = false;
	private int tag = 0;

	public EntitySetPagerAdapter(FragmentManager fragmentManager,
			EntityType entityType, int tag)
	{
		super(fragmentManager);
		this.fragmentManager = fragmentManager;
		this.entityType = entityType;
		this.tag = tag;
		logger.debug("{} pager adapter with tag {}", entityType, tag);
	}

	@Override
	public Fragment getItem(int idx)
	{
		Fragment retval = null;
		try {
			int numItems = jsonList.size();
			String json = null;

			if (idx >= numItems && !processing) {
				expandEntityListViaThread(tag);
			}
			if ((numItems=jsonList.size()) > 0 && numItems > idx) {
				retval = new EntityFragment();
				provision(retval, idx);
			}
			else {
				final Fragment f = new LoadFailureFragment();
				final int i = idx;
				retval = f;
				((LoadFailureFragment)retval).set(
					new LoadFailureFragment.Callback() {
						public void handle()
						{
							replace(f,i);
						}
				});
				loadMap.put(idx, retval);
			}
		}
		catch (Exception e) {
			logger.warn(""+e, e);
		}
		fragmentManager
			.beginTransaction()
			.add(retval, ""+entityType+idx)
			.commit();
		return(retval);
	}

	@Override
	public int getCount()
	{
		return((endOfItems) ? jsonList.size() : MAX_ENTITIES);
	}

	@Override
	public CharSequence getPageTitle(int idx)
	{
		String retval = "";
		try {
			String json = (idx < jsonList.size())
				? jsonList.get(idx)
				: "{\"name\":\"Unknown "+entityType
					+" [idx="+idx+"]\"}";
			retval = Util.get("name", json);
		}
		catch (Exception e) {
			logger.trace(""+e, e);
		}
		return(retval);
	}

	protected void expandEntityListViaThread(final int tag)
	{
		if (jsonList.size() >= MAX_ENTITIES) {
			throw new RuntimeException("maximum entities reached");
		}
		AsyncTask.execute(new Runnable() {
			public void run()
			{
				try {
					processing = true;
					expandEntityList(tag);
				}
				catch (Exception e) {
					logger.warn(""+e, e);
				}
				processing = false;
			}
		});
	}

	protected synchronized void expandEntityList(final int tag)
			throws DatastoreException, NetworkException
	{
		int page = Math.max(1,(jsonList.size() / MAX_REQ_ENTITIES)+1);
		int count = 0;

		if (!endOfItems) {
			JSONArray jarray = CrudHelper.read(entityType, page,
				MAX_REQ_ENTITIES, tag);

			for (int j=0; j < jarray.length(); j++) {
				int idx = ((page-1) * MAX_REQ_ENTITIES) + j;
				try {
					jsonList.add(
						""+jarray.getJSONObject(j));

					if (loadMap.containsKey(idx)) {
						replace(loadMap.get(idx), idx);
						loadMap.remove(idx);
					}
					count++;
				}
				catch (JSONException e) {
					throw new NetworkException(e);
				}
			}
		}
		endOfItems =  (count < MAX_REQ_ENTITIES);
	}

	protected final void replace(Fragment oldFragment, int idx)
	{
		if (jsonList.size() > idx) {
			Fragment newFragment = new EntityFragment();
			provision(newFragment, idx);
			fragmentManager
				.beginTransaction()
				.detach(oldFragment)
				.add(newFragment, ""+entityType+idx)
				.attach(newFragment)
				.commit();
		}
	}

	protected final void provision(Fragment fragment, int idx)
	{
		Bundle args = new Bundle();
		String json = jsonList.get(idx);
		args.putString(EntityFragment.ARG_JSON, json);
		args.putString(EntityFragment.ARG_ENTITY, ""+entityType);
		fragment.setArguments(args);
	}
}
