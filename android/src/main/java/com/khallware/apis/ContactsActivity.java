// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import com.khallware.apis.enums.EntityType;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.provider.ContactsContract;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContactsActivity extends FragmentActivity
{
	private static final Logger logger = LoggerFactory.getLogger(
		ContactsActivity.class);

	private EntitySetPagerAdapter entitySetPagerAdapter = null;
	private ViewPager viewPager = null;
	private int tag = 0;

	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.activity_contacts);
		bundle = (bundle == null) ? getIntent().getExtras() : bundle;
		tag = Util.resolveTag(bundle);
		entitySetPagerAdapter = new EntitySetPagerAdapter(
			getSupportFragmentManager(), EntityType.contact, tag);
		viewPager = (ViewPager)findViewById(R.id.pager);
		viewPager.setAdapter(entitySetPagerAdapter);
	}

	public void goContact(View view)
	{
		logger.trace("goContact()...");
		LinearLayout layout = (LinearLayout)view.getParent();
		goContact(
			(EditText)layout.findViewById(R.id.contact_name),
			(EditText)layout.findViewById(R.id.contact_email),
			(EditText)layout.findViewById(R.id.contact_phone),
			(EditText)layout.findViewById(R.id.contact_title),
			(EditText)layout.findViewById(R.id.contact_address),
			(EditText)layout.findViewById(R.id.contact_org));
	}

	protected void goContact(EditText... data)
	{
		try {
			lauchIntent(
				""+data[0].getText(),
				""+data[1].getText(),
				""+data[2].getText(),
				""+data[3].getText(),
				""+data[4].getText(),
				""+data[5].getText());
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
		}
	}

	protected void lauchIntent(String... values)
	{
		String[] extras = new String[] {
			ContactsContract.Intents.Insert.NAME,
			ContactsContract.Intents.Insert.EMAIL,
			ContactsContract.Intents.Insert.PHONE,
			ContactsContract.Intents.Insert.JOB_TITLE,
			ContactsContract.Intents.Insert.POSTAL,
			ContactsContract.Intents.Insert.COMPANY
		};
		int idx = 0;
		try {
			String action = ContactsContract.Intents.Insert.ACTION;
			Intent intent = new Intent(action);
			intent.setType(
				ContactsContract.RawContacts.CONTENT_TYPE);

			for (String value : values) {
				if (value != null && !value.isEmpty()
						&& !"unknown".equals(value)) {
					intent.putExtra(extras[idx], value);
				}
				idx++;
			}
			startActivity(intent);
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
		}
	}
}
