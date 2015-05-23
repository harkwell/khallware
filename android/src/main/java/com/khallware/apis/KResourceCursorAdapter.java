// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KResourceCursorAdapter extends ResourceCursorAdapter
{
	private static final Logger logger = LoggerFactory.getLogger(
		KResourceCursorAdapter.class);

	public KResourceCursorAdapter(Context context, int layout,
			Cursor cursor, int flags)
	{
		super(context, layout, cursor, flags);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		TextView textView = (TextView)view.findViewById(
			R.id.favorite_name);
		int idx = cursor.getColumnIndex("tag");
		int tagId = Integer.parseInt(cursor.getString(idx));
		String name = null;
		idx = cursor.getColumnIndex("name");
		textView.setText((name = cursor.getString(idx)));
		textView = (TextView)view.findViewById(R.id.favorite_id);
		textView.setText(""+tagId);
		logger.debug("favorite: tag={} name=\"{}\"", ""+tagId, name);
	}
}
