// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import com.khallware.apis.enums.EntityType;
import com.khallware.apis.tasks.DownloadSound;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.io.File;

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
		bundle = (bundle == null) ? getIntent().getExtras() : bundle;
		tag = Util.resolveTag(bundle);
		entitySetPagerAdapter = new EntitySetPagerAdapter(
			getSupportFragmentManager(), EntityType.sound, tag);
		viewPager = (ViewPager)findViewById(R.id.pager);
		viewPager.setAdapter(entitySetPagerAdapter);
	}

	public void goPlay(View view)
	{
		// current audio stops playing because it goes out of context
		final Context ctxt = getApplicationContext();
		LinearLayout layout = (LinearLayout)view.getParent();
		EditText editText =(EditText)layout.findViewById(R.id.sound_id);
		int soundId = 0;
		try {
			soundId = Integer.parseInt(""+editText.getText());
			new DownloadSound(new DownloadSound.Callback() {
				public void handle(File file)
				{
					Util.play(file, ctxt);
				}
			}, getFilesDir()).execute(soundId);
		}
		catch (Exception e) {
			logger.error(""+e, e);
			Util.toastException(e, ctxt);
		}
	}
}
