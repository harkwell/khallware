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
	private boolean expanding = false;
	private boolean replacing = false;
	private int index = 0;
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
	public int getCount()
	{
		int retval = jsonList.size();

		if (index == retval && !expanding) {
			if (!endOfItems) {
				expandEntityListViaThread(tag);
			}
			else if (!replacing) {
				Fragment fragment = new NoneAvailableFragment();
				provision(fragment);
				replace(getItem(index), fragment);
				retval++;
			}
		}
		/*
		else if ((retval - index) <= 1 && !replacing) {
			replace(getItem(index), index);
		}
		*/
		return(retval);
	}

	@Override
	public Fragment getItem(int idx)
	{
		Fragment retval = null;
		this.index = idx;
		try {
			int numItems = jsonList.size();
			String json = null;

			if ((numItems == 0 || idx > numItems) && endOfItems) {
				retval = new NoneAvailableFragment();
				provision(retval);
			}
			else if (numItems > 0 && numItems > idx) {
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
					expanding = true;
					expandEntityList(tag);
				}
				catch (Exception e) {
					logger.warn(""+e, e);
				}
				expanding = false;
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

	protected final void replace(Fragment oldFragment, Fragment newFragment)
	{
		replacing = true;
		fragmentManager
			.beginTransaction()
			.detach(oldFragment)
			.add(newFragment, ""+entityType+index)
			.attach(newFragment)
			.commit();
		notifyDataSetChanged(); // NOTE: will invoke getCount()
		replacing = false;
		// KDH sometimes we never get here...
	}

	protected final void replace(Fragment oldFragment, int idx)
	{
		if (jsonList.size() > idx) {
			Fragment newFragment = new EntityFragment();
			provision(newFragment, idx);
			replace(oldFragment, newFragment);
		}
	}

	protected final void provision(Fragment fragment)
	{
		provision(fragment, -1);
	}

	protected final void provision(Fragment fragment, int idx)
	{
		Bundle args = new Bundle();
		String json = (idx >= 0) ? jsonList.get(idx) : "{}";
		args.putString(EntityFragment.ARG_JSON, json);
		args.putString(EntityFragment.ARG_ENTITY, ""+entityType);
		fragment.setArguments(args);
	}
}
