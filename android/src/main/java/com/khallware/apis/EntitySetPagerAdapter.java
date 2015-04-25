// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import com.khallware.apis.enums.EntityType;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import java.util.List;
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

	private final List<String> entityList = new Vector<>();
	private EntityType entityType = null;
	private boolean endOfItems = false;
	private boolean processing = false;
	private int tag = 0;

	public EntitySetPagerAdapter(FragmentManager fragmentManager,
			EntityType entityType, int tag)
	{
		super(fragmentManager);
		this.entityType = entityType;
		this.tag = tag;
		logger.debug("{} pager adapter with tag {}", entityType, tag);
	}

	@Override
	public Fragment getItem(int idx)
	{
		Fragment retval = null;
		try {
			int numItems = entityList.size();
			Bundle args = new Bundle();
			String json = null;

			if (idx >= numItems && !processing) {
				expandEntityListViaThread(tag);
			}
			numItems = entityList.size();
			json = (numItems > 0 && numItems > idx)
				? entityList.get(idx)
				: "{}";
			retval = new EntityFragment();
			args.putString(EntityFragment.ARG_JSON, json);
			args.putString(EntityFragment.ARG_ENTITY,""+entityType);
			retval.setArguments(args);
		}
		catch (Exception e) {
			logger.warn(""+e, e);
		}
		return(retval);
	}

	@Override
	public int getCount()
	{
		return((endOfItems) ? entityList.size() : MAX_ENTITIES);
	}

	@Override
	public CharSequence getPageTitle(int idx)
	{
		String retval = "";
		try {
			String tmp = (idx < entityList.size())
				? entityList.get(idx)
				: "{\"name\":\"unknown id="+idx+"\"}";
			retval = Util.get("name", tmp);
		}
		catch (Exception e) {
			logger.trace(""+e, e);
		}
		return(retval);
	}

	protected void expandEntityListViaThread(final int tag)
	{
		if (entityList.size() >= MAX_ENTITIES) {
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
		int page = Math.max(1,(entityList.size()/MAX_REQ_ENTITIES)+1);
		int count = 0;

		if (!endOfItems) {
			JSONArray jarray = CrudHelper.read(entityType, page,
				MAX_REQ_ENTITIES, tag);

			for (int idx=0; idx < jarray.length(); idx++) {
				try {
					entityList.add(""+jarray.getJSONObject(
						idx));
					count++;
				}
				catch (JSONException e) {
					throw new NetworkException(e);
				}
			}
		}
		endOfItems =  (count < MAX_REQ_ENTITIES);
	}
}
