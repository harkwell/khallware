// Copyright Kevin D.Hall 2014-2015
/**
 * $Id$
 * ============================================================================
 * Datastore.java is a helper class for dealing with stored data
 *
 * The Android "Khallware" application
 * (C) 2014 Kevin D.Hall
 *
 * The software, processes, trade secrets and technical/business know-how used
 * on these premises are the property of Kevin D.Hall and are not to be copied,  * divulged or used without the express written consent of the author.
 */
package com.khallware.apis;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteException;
import android.database.DatabaseErrorHandler;
import android.database.Cursor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class Datastore extends SQLiteOpenHelper
{
	private static final Logger logger =
		LoggerFactory.getLogger(Datastore.class);
	public static final String DB_FILE = "khallware.db";
	public static final String DB_PATH = "/data/data/com.khallware"
		+".apis/databases/";
	public static final int DB_VERSION = 1;
	private static Datastore instance = null;
	private SQLiteDatabase handle = null;

	private Datastore(Context ctxt, SharedPreferences prefs)
	{
		super(ctxt, DB_FILE, null, DB_VERSION,
				new DatabaseErrorHandler() {
			public void onCorruption(SQLiteDatabase dbObj) {
			}
		});
	}

	public static Datastore getDatastore()
	{
		return(instance);
	}

	public static Datastore getDatastore(Context ctxt,
			SharedPreferences prefs)
	{
		if (instance != null) {
			return(instance);
		}
		instance = new Datastore(ctxt, prefs);
		return(instance);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		logger.trace("onCreate(SQLiteDatabase)ing...");
		String sql = "CREATE TABLE connect ("
			+           "url VARCHAR(255) NOT NULL, "
			+           "user VARCHAR(255) NOT NULL, "
			+           "pass VARCHAR(255) NOT NULL "
			+    ")";
		logger.debug("sql: " +sql);
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer)
	{
		logger.trace("onUpgrade()ing... from "+oldVer+" to "+newVer);
		onCreate(db);
	}

	public String[] getUrlUserPasswd() throws DatastoreException
	{
		try {
			return(getUrlUserPasswdUnwrappwed());
		}
		catch (Exception e) {
			throw new DatastoreException(e);
		}
	}

	public void setUrlUserPasswd(String[] uup) throws DatastoreException
	{
		try {
			setUrlUserPasswdUnwrapped(uup);
		}
		catch (Exception e) {
			throw new DatastoreException(e);
		}
	}

	private SQLiteDatabase handle()
	{
		if (handle == null || !handle.isOpen()) {
			handle = getWritableDatabase();
		}
		return(handle);
	}

	private String[] getUrlUserPasswdUnwrappwed() throws SQLiteException
	{
		String[] retval = new String[] { "", "", "" };
		Cursor cursor = null;
		String sql = "SELECT url, user, pass "
			+      "FROM connect";
		logger.debug("sql: " +sql);
		cursor = handle().rawQuery(sql, new String[] {});

		if (cursor.moveToNext()) {
			retval[0] = cursor.getString(
				cursor.getColumnIndexOrThrow("url"));
			retval[1] = cursor.getString(
				cursor.getColumnIndexOrThrow("user"));
			retval[2] = cursor.getString(
				cursor.getColumnIndexOrThrow("pass"));
		}
		cursor.close();
		return(retval);
	}

	private void setUrlUserPasswdUnwrapped(String[] uup)
			throws SQLiteException
	{
		String sql = "DELETE FROM connect";
		logger.debug("sql: " +sql);
		handle().execSQL(sql);

		sql =    "INSERT INTO connect (url, user, pass) "
			+"VALUES ('"+uup[0]+"','"+uup[1]+"','"+uup[2]+"')";
		handle().execSQL(sql);
	}
}
