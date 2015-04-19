// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import com.khallware.apis.enums.EntityType;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.EditText;
import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventsActivity extends FragmentActivity
{
	private static final Logger logger = LoggerFactory.getLogger(
		EventsActivity.class);

	private EntitySetPagerAdapter entitySetPagerAdapter = null;
	private ViewPager viewPager = null;
	private int tag = 0;

	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.activity_events);
		bundle = (bundle == null) ? getIntent().getExtras() : bundle;
		tag = (bundle == null)
			? 0 // ideally from database
			: Integer.parseInt(""+bundle.get(Khallware.ARG_TAG));
		entitySetPagerAdapter = new EntitySetPagerAdapter(
			getSupportFragmentManager(), EntityType.event, tag);
		viewPager = (ViewPager)findViewById(R.id.pager);
		viewPager.setAdapter(entitySetPagerAdapter);
	}

	public void goCalendar(View view)
	{
		logger.trace("goCalendar()...");
		EditText editText = null;
		String token = "";
		String title = "";
		String desc = "";
		long start = 0;
		long end = 0;
		editText = (EditText)view.findViewById(R.id.event_name);
		title = ""+editText.getText();
		editText = (EditText)view.findViewById(R.id.event_desc);
		desc = ""+editText.getText();
		editText = (EditText)view.findViewById(R.id.event_start);
		token = ""+editText.getText();
		start = Long.parseLong(token);
		editText = (EditText)view.findViewById(R.id.event_end);
		token = ""+editText.getText();
		end = Long.parseLong(token);
		lauchIntent(title, desc, start, end);
	}

	protected void lauchIntent(String title, String desc, long start,
			long end)
	{
		try {
			final Intent intent = new Intent(Intent.ACTION_EDIT);
			intent.setType("vnd.android.cursor.item/event");
			intent.putExtra("title", title);
			intent.putExtra("description", desc);
			intent.putExtra("beginTime", start);
			intent.putExtra("endTime", end);
			startActivity(intent);
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
		}
	}
}
