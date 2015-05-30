// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis.tasks;

import com.khallware.apis.enums.EntityType;
import com.khallware.apis.NetworkException;
import com.khallware.apis.Util;
import android.os.AsyncTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUpload extends AsyncTask<File, Void, Void>
{
	public static final String IMAGE = "image";
	public static final String FILECOMMENT = "filecomment";
	public static final String BOUNDARY = "----------------------khallware";
	public static final Map<EntityType,String> mimeMap = new HashMap<>();
	private static final Logger logger = LoggerFactory.getLogger(
		FileUpload.class);

	static {
		mimeMap.put(EntityType.photo, "image/jpeg");
	};

	private List<NetworkException> errors = new ArrayList<>();
	private EntityType type = null;
	private String url = null;

	public FileUpload(EntityType type, String url)
	{
		this.type = type;
		this.url = url;
	}

	public List<NetworkException> getErrors()
	{
		return(errors);
	}

	@Override
	protected Void doInBackground(File... files)
	{
		try {
			for (File file : files) {
				perform(type, url, file);
			}
		}
		catch (NetworkException e) {
			errors.add(e);
		}
		return((Void)null);
	}

	public static void perform(EntityType type, String url, File file)
			throws NetworkException
	{
		HttpURLConnection conn = null;
		try {
			String contentType = mimeMap.get(type);
			DataOutputStream request = null;
			InputStream response = null;
			contentType = (contentType == null)
				? "octet/stream"
				: contentType;
			conn = (HttpURLConnection)new URL(url).openConnection();
			conn.setUseCaches(false);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			setHeaders(conn);
			conn.setRequestProperty("Accept-Encoding", "identity");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Cache-Control", "no-cache");
			conn.setRequestProperty("Content-Type",
				"multipart/form-data; boundary="+BOUNDARY);

			request = new DataOutputStream(conn.getOutputStream());
			request.writeBytes("--"+BOUNDARY+"\r\n");
			request.writeBytes("Content-Disposition: form-data; "
				+"name=\"" +FILECOMMENT +"\"\r\n\r\n");
			request.writeBytes(file.getName());
			request.writeBytes("\r\n");
			request.writeBytes("--"+BOUNDARY+"\r\n");

			request.writeBytes("Content-Disposition: form-data; "
				+"name=\"" +IMAGE +"\"; "
				+"filename=\"" +file.getName() +"\"\r\n");
			request.writeBytes("Content-Type: "
				+contentType+"\r\n\r\n");
			request.write(readContent(file));
			request.writeBytes("\r\n");
			request.writeBytes("--"+BOUNDARY+"--\r\n");
			request.flush();
			request.close();

			logger.trace(Util.toString(
				response = conn.getInputStream()));
			response.close();
		}
		catch (Exception e) {
			throw new NetworkException(e);
		}
		finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	protected static void setHeaders(HttpURLConnection conn)
	{
		Map<String, String> map = Util.defaultHeadersAsMap();

		for (String key : map.keySet()) {
			conn.setRequestProperty(key, map.get(key));
		}
	}

	public static byte[] readContent(File file) throws IOException
	{
		byte[] retval = new byte[(int)file.length()];
		FileInputStream fis = new FileInputStream(file);
		fis.read(retval);
		fis.close();
		return(retval);
	}
}
