// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import android.widget.EditText;
import android.widget.Toast;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectActivity extends Activity
{
	private static final Logger logger = LoggerFactory.getLogger(
		ConnectActivity.class);

	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.connect);
	}

	public void doConnect(View view)
	{
		String url = null;
		String[] uup = new String[] {"", "", ""};
		EditText editText = null;
		String proto = "http://";
		logger.trace("doConnect()...");
		try {
			editText = (EditText)findViewById(R.id.connect_host);
			url = editText.getText().toString();
			proto = (url.startsWith("http")) ? "" : proto;
			uup[0] = proto+url;
			editText = (EditText)findViewById(R.id.connect_user);
			uup[1] = editText.getText().toString();
			editText = (EditText)findViewById(R.id.connect_pass);
			uup[2] = editText.getText().toString();
			testConnectionAndFinish(uup);
		}
		catch (Exception e) {
			Util.toastException(e, getApplicationContext());
		}
	}

	private void testConnectionAndFinish(final String[] uup)
	{
		final Datastore dstore = Datastore.getDatastore();
		AsyncTask.execute(new Runnable() {
			public void run()
			{
				String[] prev = null;
				try {
					prev = dstore.getUrlUserPasswd();
					dstore.setUrlUserPasswd(uup);
					CrudHelper.getParentTag(1);
					dstore.setUrlUserPasswd(uup);
					dstore.truncateFavorites();
					finish();
				}
				catch (Exception e) {
					logger.warn(""+e, e);
					try {
						dstore.setUrlUserPasswd(prev);
					}
					catch (DatastoreException ex) {
						logger.error(""+ex, ex);
					}
					runOnUiThread(new Runnable() {
						public void run()
						{
							failConnect();
						}
					});
				}
			}
		});
	}

	private void failConnect()
	{
		try {
			Toast toast = Toast.makeText(getApplicationContext(),
				"Could not connect...", Toast.LENGTH_SHORT);
			toast.show();
		}
		catch (Exception e) {
			logger.error(""+e, e);
		}
	}
}
