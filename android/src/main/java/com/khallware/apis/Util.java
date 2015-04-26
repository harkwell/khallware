// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;
import android.content.Context;
// httpclient is built into Android
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util
{
	private static final Logger logger = LoggerFactory.getLogger(
		Util.class);

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
		catch (Exception e) {
			logger.warn("invalid json returned: ({})", tmp);
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
}
