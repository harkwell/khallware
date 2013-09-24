// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import android.util.Base64;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.HttpResponse;
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
		Map<String, String> map = new HashMap<>();

		for (String header : headers) {
			String[] pair = header.split(":");

			if (pair.length != 2) {
				continue;
			}
			map.put(pair[0], pair[1]);
		}
		return(queryREST(request, map));
	}

	public static JSONObject queryREST(HttpRequestBase request,
			Map<String, String> headers) throws NetworkException
	{
		JSONObject retval = null;
		HttpResponse response = null;
		DefaultHttpClient client = new DefaultHttpClient(
			new BasicHttpParams());
		String tmp = null;

		for (String header : headers.keySet()) {
			request.setHeader(header, headers.get(header));
		}
		try {
			response = client.execute(request);
			tmp = toString(response.getEntity().getContent());
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
		String retval = "";
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			for (int b=-1; (b=is.read()) != -1;) {
				bos.write(b);
			}
			is.close();
			bos.close();
			retval = ""+bos;
		}
		catch (IOException e) {
			logger.error(""+e, e);
		}
		return(retval);
	}

	public static String getBasicAuthHeader() throws DatastoreException
	{
		StringBuilder retval = new StringBuilder();
		String[] info = Datastore.getDatastore().getUrlUserPasswd();
		String tmp = info[1]+":"+info[2];
		retval.append("Authorization:Basic ");
		tmp = Base64.encodeToString(tmp.getBytes(), Base64.DEFAULT);
		retval.append(tmp);
		return(""+retval);
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
}
