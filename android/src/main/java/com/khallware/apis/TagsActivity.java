// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import com.khallware.apis.enums.EntityType;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.content.Context;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;
import android.os.Bundle;
import android.os.AsyncTask;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class TagsActivity extends FragmentActivity
{
	private static final Logger logger = LoggerFactory.getLogger(
		TagsActivity.class);

	private EntitySetPagerAdapter entitySetPagerAdapter = null;
	private ViewPager viewPager = null;
	private int tag = 0;

	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.activity_tags);
		bundle = (bundle == null) ? getIntent().getExtras() : bundle;
		tag = (bundle == null)
			? 0 // ideally from database
			: Integer.parseInt(""+bundle.get(Khallware.ARG_TAG));
		entitySetPagerAdapter = new EntitySetPagerAdapter(
			getSupportFragmentManager(), EntityType.tag, tag);
		viewPager = (ViewPager)findViewById(R.id.pager);
		viewPager.setAdapter(entitySetPagerAdapter);
	}

	public void goParent(View view)
	{
		logger.trace("goParent()...");
		AsyncTask.execute(new Runnable() {
			public void run()
			{
				try {
					lauchIntent(
						TagsActivity.class,
						CrudHelper.getParentTag(
							tag).getInt("id"));
				}
				catch (Exception e) {
					toastException(e);
				}
			}
		});
	}

	public void goChildren(View view)
	{
		logger.trace("goChildren()...");
		EditText editText = (EditText)view.findViewById(R.id.tag_id);
		int child = Integer.parseInt(""+editText.getText());
		lauchIntent(TagsActivity.class, child);
	}

	public void goTag(View view)
	{
		logger.trace("goTag()...");
		lauchIntent(Khallware.class, tag);
	}

	protected void lauchIntent(Class clazz, int tagId)
	{
		try {
			Intent intent = new Intent(this, clazz);
			intent.putExtra(Khallware.ARG_TAG, ""+tagId);
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
}
