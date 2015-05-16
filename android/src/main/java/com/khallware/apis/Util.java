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
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.media.MediaPlayer;
// httpclient is built into Android
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util
{
	private static final Logger logger = LoggerFactory.getLogger(
		Util.class);

	public static final Map<EntityType, String> mimeMap = new HashMap<>();

	static {
		mimeMap.put(EntityType.photo, "image/jpeg");
	}

	// KDH: is this right?
	public static JSONObject queryREST(HttpRequestBase request,
			String[] headers) throws NetworkException
	{
		return(queryREST(request, defaultHeadersAsMap()));
	}

	/**
	 * http://stackoverflow.com/questions/10650660/\
	 *    android-bitmapfactory-decodestream-returns-null
	 */
	public static InputStream queryRESTasStream(HttpRequestBase request,
			Map<String, String> headers) throws NetworkException
	{
		InputStream retval = null;
		HttpResponse response = null;
		BufferedHttpEntity entity = null;
		DefaultHttpClient client = new DefaultHttpClient(
			new BasicHttpParams());

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

	public static JSONObject queryREST(HttpRequestBase request,
			Map<String, String> headers) throws NetworkException
	{
		JSONObject retval = null;
		String tmp = null;
		try {
			tmp = toString(queryRESTasStream(request, headers));
			retval = new JSONObject(tmp);
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
			logger.warn("invalid json returned: ({})", tmp);
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
		return(handleGet(uri, defaultHeaders()));
	}

	public static JSONObject handleGet(String uri, String[] headers)
			throws NetworkException
	{
		return(queryREST(new HttpGet(uri), headers));
	}

	public static JSONObject handlePost(String uri, JSONObject body)
			throws NetworkException
	{
		return(handlePost(uri, body, defaultHeaders()));
	}

	public static HttpRequestBase setContent(
			HttpEntityEnclosingRequestBase retval, JSONObject json)
	{
		final BasicHttpEntity entity = new BasicHttpEntity();
		ByteArrayInputStream bis = new ByteArrayInputStream(
			json.toString().getBytes());
		entity.setContent(bis);
		retval.setEntity(entity);
		return(retval);
	}

	/*
	public static JSONObject handlePost(EntityType type, int tagId,
			File file) throws NetworkException, DatastoreException
	{
		String[] uup = Datastore.getDatastore().getUrlUserPasswd();
		String url = uup[0]+"/apis/v1/upload?tagId="+tagId;
		// HttpClient client = new DefaultHttpClient(
		DefaultHttpClient client = new DefaultHttpClient(
			new BasicHttpParams());
		// client.getParams().setParameter(
		//	CoreProtocolPNames.PROTOCOL_VERSION,
		//	HttpVersion.HTTP_1_1);
		HttpPost request = new HttpPost(url);
		MultipartEntity entity = new MultipartEntity();
		// ContentBody contentBody = new FileBody(file);
		ContentBody contentBody = new FileBody(file, mimeMap.get(type));
		try {
			entity.addPart("filecomment", new StringBody(""+type));
			entity.addPart("image", contentBody);
		}
		catch (Exception e) {
			throw new NetworkException(e);
		}
		request.setEntity(entity);
		return(queryREST(request, defaultHeadersAsMap()));
	}
	*/

	public static JSONObject handlePost(String uri, JSONObject body,
			String[] headers) throws NetworkException
	{
		return(queryREST(setContent(new HttpPost(uri), body), headers));
	}

	public static JSONObject handlePut(String uri, JSONObject body)
			throws NetworkException
	{
		return(handlePut(uri, body, defaultHeaders()));
	}

	public static JSONObject handlePut(String uri, JSONObject body,
			String[] headers) throws NetworkException
	{
		return(queryREST(setContent(new HttpPut(uri), body), headers));
	}

	public static JSONObject handleDelete(String uri)
			throws NetworkException
	{
		return(handleDelete(uri, defaultHeaders()));
	}

	public static JSONObject handleDelete(String uri, String[] headers)
			throws NetworkException
	{
		return(queryREST(new HttpDelete(uri), headers));
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

	public static Map<String, String> map = new HashMap<>();

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
}
