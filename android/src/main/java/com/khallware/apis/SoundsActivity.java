// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import com.khallware.apis.enums.EntityType;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoundsActivity extends FragmentActivity
{
	private static final Logger logger = LoggerFactory.getLogger(
		SoundsActivity.class);

	private EntitySetPagerAdapter entitySetPagerAdapter = null;
	private ViewPager viewPager = null;
	private int tag = 0;

	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.activity_sounds);
		tag = bundle.getInt(Khallware.ARG_TAG);
		entitySetPagerAdapter = new EntitySetPagerAdapter(
			getSupportFragmentManager(), EntityType.sound, tag);
		viewPager = (ViewPager)findViewById(R.id.pager);
		viewPager.setAdapter(entitySetPagerAdapter);
	}
}
