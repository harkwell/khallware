// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import com.khallware.apis.enums.EntityType;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;
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
		tag = Util.resolveTag(bundle);
		entitySetPagerAdapter = new EntitySetPagerAdapter(
			getSupportFragmentManager(), EntityType.event, tag);
		viewPager = (ViewPager)findViewById(R.id.pager);
		viewPager.setAdapter(entitySetPagerAdapter);
	}

	public void goCalendar(View view)
	{
		logger.trace("goCalendar()...");
		LinearLayout layout = (LinearLayout)view.getParent();
		goCalendar(
			(EditText)layout.findViewById(R.id.event_name),
			(EditText)layout.findViewById(R.id.event_desc),
			(EditText)layout.findViewById(R.id.event_start),
			(EditText)layout.findViewById(R.id.event_end));
	}

	protected void goCalendar(EditText... data)
	{
		try {
			lauchIntent(
				""+data[0].getText(),
				""+data[1].getText(),
				Long.parseLong(""+data[2].getText()),
				Long.parseLong(""+data[2].getText()));
				// KDH Long.parseLong(""+data[3].getText()));
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
		}
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
