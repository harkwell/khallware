// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import com.khallware.apis.enums.EntityType;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
// import java.nio.file.Files;
import android.os.Bundle;
import android.net.Uri;
import android.util.Base64;
import android.widget.Toast;
import android.content.Context;
import android.database.Cursor;
import android.accounts.AccountManager;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.media.MediaPlayer;
// httpmime is not built into Android, but needs >= apache httpcore-4.3
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
// httpclient is built into Android
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util
{
	private static final Logger logger = LoggerFactory.getLogger(
		Util.class);

	public static final Map<EntityType, String> mimeMap = new HashMap<>();
	public static Map<String, String> map = new HashMap<>();

	static {
		mimeMap.put(EntityType.photo, "image/jpeg");
	};
	static {
		map.put("uid", ContactsContract.Contacts._ID);
		map.put("name", ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
		map.put("phone", CommonDataKinds.Phone.NUMBER);
		map.put("email", CommonDataKinds.Email.ADDRESS );
		map.put("org", CommonDataKinds.Organization.COMPANY );
		map.put("title", CommonDataKinds.Organization.TITLE );
		map.put("address",
			CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS );
	};

	public static JSONObject queryREST(HttpRequestBase request,
			HttpClient... varargs) throws NetworkException
	{
		HttpClient client = (varargs.length > 0)
			? varargs[0]
			: new DefaultHttpClient(new BasicHttpParams());
		return(queryREST(request, defaultHeadersAsMap(), client));
	}

	/**
	 * http://stackoverflow.com/questions/10650660/\
	 *    android-bitmapfactory-decodestream-returns-null
	 */
	public static InputStream queryRESTasStream(HttpRequestBase request,
			Map<String, String> headers, HttpClient client)
			throws NetworkException
	{
		InputStream retval = null;
		HttpResponse response = null;
		BufferedHttpEntity entity = null;
		client = (client == null)
			? new DefaultHttpClient(new BasicHttpParams())
			: client;

		for (String header : headers.keySet()) {
			request.setHeader(header, headers.get(header));
		}
		try {
			response = client.execute(request);
			entity = new BufferedHttpEntity(response.getEntity());
			retval = entity.getContent();

			if (retval.markSupported()) {
				retval.mark(1024);
			}
			request.abort();
		}
		catch (IOException e) {
			throw new NetworkException(e);
		}
		return(retval);
	}

	/**
	 * Return the JSONObject representation of a tag from the web service
	 * or null if not found.  Throw a NetworkException if appropriate.
	 */
	public static JSONObject queryREST(HttpRequestBase request,
			Map<String, String> headers, HttpClient client)
			throws NetworkException
	{
		JSONObject retval = null;
		String rslt = "";
		try {
			rslt = toString(
				queryRESTasStream(request, headers, client));
			retval = new JSONObject(rslt);
		}
		catch (JSONException e) {
			logger.warn(""+e, e);
			try {
				retval =new JSONObject("{\"error\":\""+e+"\"}");
			}
			catch (Exception ie) {
				logger.error(""+ie, ie);
			}
		}
		catch (Exception e) {
			logger.warn("invalid json returned: ({})", rslt);
			logger.warn(""+e, e);
			throw new NetworkException(e);
		}
		return(retval);
	}

	public static String toString(InputStream is)
	{
		StringBuilder retval = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(is));
			retval.append(reader.readLine()).append("\n");
			reader.close();
		}
		catch (IOException e) {
			logger.error(""+e, e);
		}
		return(""+retval);
	}

	public static boolean toFile(File file, InputStream is)
	{
		boolean retval = false;
		try {
			FileOutputStream fos = new FileOutputStream(file);

			for (int ch=-1; (ch = is.read()) != -1;) {
				fos.write(ch);
			}
			fos.close();
			retval = true;
		}
		catch (IOException e) {
			logger.error(""+e, e);
		}
		return(retval);
	}

	/*
	public static boolean toFileJava7(File file, InputStream is)
	{
		boolean retval = false;
		try {
			Files.write(file.toPath(), toString(is).getBytes());
			retval = true;
		}
		catch (IOException e) {
			logger.error(""+e, e);
		}
		return(retval);
	}
	*/

	public static String getBasicAuthHeader() throws DatastoreException
	{
		StringBuilder retval = new StringBuilder();
		String[] uup = Datastore.getDatastore().getUrlUserPasswd();
		String tmp = uup[1]+":"+uup[2];
		retval.append("Authorization:Basic ");
		tmp = Base64.encodeToString(tmp.getBytes(), Base64.NO_WRAP);
		retval.append(tmp);
		return(""+retval);
	}

	public static Map<String, String> defaultHeadersAsMap()
	{
		Map<String, String> retval = new HashMap<>();

		for (String header : defaultHeaders()) {
			String[] pair = header.split(":");

			if (pair.length != 2) {
				continue;
			}
			retval.put(pair[0], pair[1]);
		}
		return(retval);
	}

	public static String[] defaultHeaders()
	{
		String basicAuthHeader = "";
		try {
			basicAuthHeader = getBasicAuthHeader();
		}
		catch (DatastoreException e) {
			logger.error(""+e, e);
		}
		return(new String[] {
			"Content-Type:application/json",
			"Accept:application/json",
			basicAuthHeader
		});
	}

	public static JSONObject handleGet(String uri) throws NetworkException
	{
		return(queryREST(new HttpGet(uri)));
	}

	public static HttpRequestBase setContent(
			HttpEntityEnclosingRequestBase retval, JSONObject json)
	{
		return(setContent(retval, ""+json));
	}

	public static HttpRequestBase setContent(
			HttpEntityEnclosingRequestBase retval, String json)
	{
		final BasicHttpEntity entity = new BasicHttpEntity();
		ByteArrayInputStream bis = new ByteArrayInputStream(
			json.getBytes());
		entity.setContent(bis);
		retval.setEntity(entity);
		return(retval);
	}

	/**
	 * BUG:
	 * The stock version of apache httpclient in android is stuck at
	 * 4.0beta2 and does not include ContentType.java, so this code throws
	 * a class cast exception.  (20150527) working to find a way to
	 * include httpcore/httpmime for httpclient 4.3.5.1...
	 */
	public static JSONObject handlePost(EntityType type, int tagId,
			File file) throws NetworkException, DatastoreException
	{
		String[] uup = Datastore.getDatastore().getUrlUserPasswd();
		String url = uup[0]+"/apis/v1/upload?tagId="+tagId;
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(url);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create()
			.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
			.addTextBody("filecomment", ""+type)
			.addBinaryBody(file.getName(), file);
		request.setEntity(builder.build());
		return(queryREST(request, client));
	}

	public static JSONObject handlePost(String uri, String body)
			throws NetworkException
	{
		return(queryREST(setContent(new HttpPost(uri), body)));
	}

	public static JSONObject handlePost(String uri, JSONObject body)
			throws NetworkException
	{
		return(queryREST(setContent(new HttpPost(uri), body)));
	}

	public static JSONObject handlePut(String uri, JSONObject body)
			throws NetworkException
	{
		return(queryREST(setContent(new HttpPut(uri), body)));
	}

	public static JSONObject handleDelete(String uri)
			throws NetworkException
	{
		return(queryREST(new HttpDelete(uri)));
	}

	public static String toStringWithStacktrace(Exception exception)
	{
		StringBuilder retval = new StringBuilder();
		ByteArrayOutputStream bos = null;
		retval.append(exception.getMessage());
		bos = new ByteArrayOutputStream();
		exception.printStackTrace(new PrintStream(bos));
		retval.append("\n");
		retval.append(""+bos);
		return(""+retval);
	}

	public static String get(String key, String json) throws JSONException
	{
		String retval = "";
		JSONObject jsonObj = new JSONObject(json);

		if (jsonObj.has(key)) {
			retval = jsonObj.getString(key);
		}
		return(retval);
	}

	public static void toastException(Exception e, Context context)
	{
		int duration = Toast.LENGTH_SHORT;
		String msg = toStringWithStacktrace(e);
		Toast toast = Toast.makeText(context, msg, duration);
		logger.error(""+e, e);
		toast.show();
	}

	public static int resolveTag(Bundle bundle)
	{
		int retval = 0;
		try {
			retval = (bundle == null)
				? Datastore.getDatastore().getTag()
				: Integer.parseInt(""+bundle.get(
					Khallware.ARG_TAG));
		}
		catch (DatastoreException e) {
			logger.error(""+e, e);
		}
		return(retval);
	}

	public static void play(File file, Context ctxt)
	{
		MediaPlayer player = null;
		try {
			player = MediaPlayer.create(ctxt, Uri.fromFile(file));

			if (player != null) {
				player.start();
				// player.release();
			}
		}
		catch (Exception e) {
			Util.toastException(e, ctxt);
		}
	}

	public static JSONObject merge(Context ctxt, JSONObject retval, Uri uri,
			String id) throws JSONException
	{
		String patt = CommonDataKinds.Phone.CONTACT_ID
			+" = ?";
		Cursor cursor = ctxt.getContentResolver().query(uri, null,
			patt, new String[] { id }, null);
		int idx = -1;

		while (cursor.moveToNext()) {
			for (String key : map.keySet()) {
				if ((idx = cursor.getColumnIndex(map.get(key)))
						!= -1) {
					retval.put(key, cursor.getString(idx));
				}
			}
		}
		cursor.close();
		return(retval);
	}

	public static void postContacts(Context ctxt, int tagId)
			throws JSONException, DatastoreException,
			NetworkException
	{
		List<String> errors = new ArrayList<>();
		Cursor cursor = ctxt.getContentResolver().query(
			ContactsContract.Contacts.CONTENT_URI, null, null,
				null, null);
		Uri[] uris = new Uri[] {
			CommonDataKinds.Phone.CONTENT_URI,
			CommonDataKinds.Email.CONTENT_URI,
			// CommonDataKinds.Contactables.CONTENT_URI,
			CommonDataKinds.StructuredPostal.CONTENT_URI
		};
		while (cursor.moveToNext()) {
			JSONObject jsonObj = new JSONObject();
			int idx = cursor.getColumnIndex(map.get("uid"));
			String uid = cursor.getString(idx);
			jsonObj.put("uid", uid);
			idx = cursor.getColumnIndex(map.get("name"));
			jsonObj.put("name", cursor.getString(idx));

			for (Uri uri : uris) {
				merge(ctxt, jsonObj, uri, uid);
			}
			logger.info("json: ({})", ""+jsonObj);
			try {
				CrudHelper.create(EntityType.contact, jsonObj,
					tagId);
			}
			catch (Exception e) {
				errors.add(""+e);
			}
		}
		cursor.close();

		if (errors.size() > 0) {
			throw new NetworkException(""+errors);
		}
	}

	/**
	 * KDH: This method is too simple...
	 * developer.android.com/guide/topics/providers/contacts-provider.html
	 */
	public static void truncateContacts(Context ctxt)
			throws NetworkException
	{
		List<String> errors = new ArrayList<>();
		Cursor cursor = ctxt.getContentResolver().query(
			ContactsContract.Contacts.CONTENT_URI, null, null,
				null, null);
		String ckey = ContactsContract.Contacts.LOOKUP_KEY;
		Uri curi = ContactsContract.Contacts.CONTENT_LOOKUP_URI;

		while (cursor.moveToNext()) {
			try {
				int idx = cursor.getColumnIndex(ckey);
				String key = cursor.getString(idx);
				Uri uri = Uri.withAppendedPath(curi, key);
				logger.info("removing contact: ({})", key);
				ctxt.getContentResolver().delete(uri,null,null);
			}
			catch (Exception e) {
				errors.add(""+e);
			}
		}
		cursor.close();

		if (errors.size() > 0) {
			throw new NetworkException(""+errors);
		}
	}

	public static List<ContentProviderOperation> prepAddContactViaBatch(
			int idx, JSONObject jsonObj) throws JSONException,
			DatastoreException, NetworkException
	{
		List<ContentProviderOperation> retval = new ArrayList<>();
		ContentProviderOperation.Builder builder = null;
		String mime = null;

		// The account record
		builder = ContentProviderOperation.newInsert(
				ContactsContract.RawContacts.CONTENT_URI);
		builder.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,
			AccountManager.KEY_ACCOUNT_TYPE);
		builder.withValue(ContactsContract.RawContacts.ACCOUNT_NAME,
			AccountManager.KEY_ACCOUNT_NAME);
		retval.add(builder.build());

		// The contact name
		String[] name = parseName((jsonObj.has("name"))
			? jsonObj.getString("name")
			: "");
		mime = CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE;
		builder = ContentProviderOperation.newInsert(
			ContactsContract.Data.CONTENT_URI);
		builder.withValueBackReference(
			ContactsContract.Data.RAW_CONTACT_ID, idx);
		builder.withValue(ContactsContract.Data.MIMETYPE, mime);
		builder.withValue(ContactsContract.Data.DISPLAY_NAME_PRIMARY,
			name[0]+" "+((name[1].isEmpty()) ? "" : name[1]+" ")
			+name[2]);
		retval.add(builder.build());

		// Phone Number
		mime = CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
		builder = ContentProviderOperation.newInsert(
			ContactsContract.Data.CONTENT_URI);
		builder.withValueBackReference(
			ContactsContract.Data.RAW_CONTACT_ID, idx);
		builder.withValue(ContactsContract.Data.MIMETYPE, mime);
		builder.withValue(CommonDataKinds.Phone.TYPE,
			CommonDataKinds.Phone.TYPE_HOME);
		builder.withValue(CommonDataKinds.Phone.NUMBER,
			jsonObj.has("phone")
				? jsonObj.getString("phone")
				: "");
		retval.add(builder.build());

		// Email Address
		mime = CommonDataKinds.Email.CONTENT_ITEM_TYPE;
		builder = ContentProviderOperation.newInsert(
			ContactsContract.Data.CONTENT_URI);
		builder.withValueBackReference(
			ContactsContract.Data.RAW_CONTACT_ID, idx);
		builder.withValue(ContactsContract.Data.MIMETYPE, mime);
		builder.withValue(CommonDataKinds.Email.TYPE,
			CommonDataKinds.Email.TYPE_WORK);
		builder.withValue(CommonDataKinds.Email.DATA,
			jsonObj.has("email")
				? jsonObj.getString("email")
				: "");
		retval.add(builder.build());
		return(retval);
	}

	/**
	 * Given a token, determine first, middle and last names.
	 */
	public static String[] parseName(String token)
	{
		String[] retval = new String[] { "", "", "" };
		String[] dat = token.replaceFirst("\\s+?,",",").split(" ");

		switch (dat.length) {
		case 1:
			retval[0] = dat[0];
			break;
		case 3:
			retval[1] = (dat[0].contains(","))
				? dat[2]
				: dat[1];
		case 2:
			retval[0] = (dat[0].contains(","))
				? dat[1]
				: dat[0];
			retval[2] = (dat[0].contains(","))
				? dat[0].substring(0, dat[0].indexOf(","))
				: dat[1];
			break;
		}
		retval[0] = (retval[0].isEmpty()) ? "unknown" : retval[0];
		return(retval);
	}

	public static void replaceContacts(Context ctxt, int id)
			throws JSONException, DatastoreException,
			NetworkException
	{
		List<String> errors = new ArrayList<>();
		ArrayList<ContentProviderOperation> batch = new ArrayList<>();
		long count = CrudHelper.count(EntityType.contact, id);
		JSONArray jsonArray = CrudHelper.getContacts(0, (int)count, id);
		truncateContacts(ctxt);

		for (int idx=0; idx < jsonArray.length(); idx++) {
			try {
				batch.addAll(
					prepAddContactViaBatch(
						idx,
						jsonArray.getJSONObject(idx)));
			}
			catch (Exception e) {
				errors.add(""+e);
			}
		}
		if (batch.size() > 0) {
			try {
				ctxt.getContentResolver().applyBatch(
					ContactsContract.AUTHORITY, batch);
			}
			catch (Exception e) {
				errors.add(""+e);
			}
		}
		if (errors.size() > 0) {
			throw new NetworkException(""+errors);
		}
	}

	public static void postEvents(Context ctxt, int tagId)
			throws JSONException, DatastoreException,
			NetworkException
	{
		List<String> errors = new ArrayList<>();
		Map<String,String> map = new HashMap<>();
		Cursor cursor = null;
		map.put("title","name");
		map.put("description","description");
		map.put("dtstart","start");
		map.put("dtend","end");
		map.put("calendar_id","uid");
		cursor = ctxt.getContentResolver().query(
			Uri.parse("content://com.android.calendar/events"),
			new ArrayList<String>(map.keySet()).toArray(
				new String[map.keySet().size()]),
			null, null, null, null);

		while (cursor.moveToNext()) {
			JSONObject jsonObj = new JSONObject();

			for (String key : map.keySet()) {
				int idx = cursor.getColumnIndex(key);
				String val = cursor.getString(idx);
				jsonObj.put(map.get(key), val);
			}
			logger.info("json: ({})", ""+jsonObj);
			try {
				CrudHelper.create(EntityType.event, jsonObj,
					tagId);
			}
			catch (Exception e) {
				errors.add(""+e);
			}
		}
		cursor.close();

		if (errors.size() > 0) {
			throw new NetworkException(""+errors);
		}
	}

	public static void replaceEvents(Context ctxt, int id)
			throws JSONException, DatastoreException,
			NetworkException
	{
		List<String> errors = new ArrayList<>();
		long count = CrudHelper.count(EntityType.event, id);
		JSONArray jsonArray = CrudHelper.getEvents(0, (int)count, id);
		// truncateEvents(ctxt);
		errors.add("not yet implemented!");

		if (errors.size() > 0) {
			throw new NetworkException(""+errors);
		}
	}
}
