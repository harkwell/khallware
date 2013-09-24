// Copyright Kevin D.Hall 2014-2015

package com.khallware.apis;

import com.khallware.apis.enums.EntityType;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrudHelper
{
	private static final Logger logger = LoggerFactory.getLogger(
		CrudHelper.class);

	public static JSONObject read(EntityType type, int id)
			throws DatastoreException, NetworkException
	{
		String[] info = Datastore.getDatastore().getUrlUserPasswd();
		String url = info[0]+"/apis/v1/"+type+"s/"+id;
		logger.debug("GET {}", url);
		return(Util.handleGet(url));
	}

	public static JSONArray read(EntityType type, int page, int pageSize,
			int tag) throws DatastoreException, NetworkException
	{
		JSONArray retval = null;
		JSONObject json = null;
		String[] info = Datastore.getDatastore().getUrlUserPasswd();
		String key = ""+type+"s";
		String url = info[0]+"/apis/v1/"+type+"s?page="+page;
		url += "&pageSize="+pageSize+"&tagId="+tag;
		logger.debug("GET {}", url);

		if ((json = Util.handleGet(url)) == null) {
			logger.warn("no json returned via GET {}", url);
		}
		else if (json.length()>0 && json.has(key) && !json.isNull(key)){
			try {
				retval = json.getJSONArray(key);
			}
			catch (JSONException e) {
				throw new NetworkException(e);
			}
		}
		return(retval);
	}

	public static JSONObject getBookmark(int id) throws DatastoreException,
			NetworkException
	{
		return(read(EntityType.bookmark, id));
	}

	public static JSONArray getBookmarks(int from, int to, int tag)
			throws DatastoreException, NetworkException
	{
		return(read(EntityType.bookmark, from, to, tag));
	}

	public static JSONObject getContact(int id) throws DatastoreException,
			NetworkException
	{
		return(read(EntityType.contact, id));
	}

	public static JSONArray getContacts(int from, int to, int tag)
			throws DatastoreException, NetworkException
	{
		return(read(EntityType.contact, from, to, tag));
	}

	public static JSONObject getVideo(int id) throws DatastoreException,
			NetworkException
	{
		return(read(EntityType.video, id));
	}

	public static JSONArray getVideos(int from, int to, int tag)
			throws DatastoreException, NetworkException
	{
		return(read(EntityType.video, from, to, tag));
	}

	public static JSONObject getSound(int id) throws DatastoreException,
			NetworkException
	{
		return(read(EntityType.sound, id));
	}

	public static JSONArray getSounds(int from, int to, int tag)
			throws DatastoreException, NetworkException
	{
		return(read(EntityType.sound, from, to, tag));
	}

	public static JSONObject getEvent(int id) throws DatastoreException,
			NetworkException
	{
		return(read(EntityType.event, id));
	}

	public static JSONArray getEvents(int from, int to, int tag)
			throws DatastoreException, NetworkException
	{
		return(read(EntityType.event, from, to, tag));
	}

	public static JSONObject getLocation(int id) throws DatastoreException,
			NetworkException
	{
		return(read(EntityType.location, id));
	}

	public static JSONArray getLocations(int from, int to, int tag)
			throws DatastoreException, NetworkException
	{
		return(read(EntityType.location, from, to, tag));
	}

	public static JSONObject getFileItem(int id) throws DatastoreException,
			NetworkException
	{
		return(read(EntityType.fileitem, id));
	}

	public static JSONArray getFileItems(int from, int to, int tag)
			throws DatastoreException, NetworkException
	{
		return(read(EntityType.fileitem, from, to, tag));
	}

	public static JSONObject getBlog(int id) throws DatastoreException,
			NetworkException
	{
		return(read(EntityType.blog, id));
	}

	public static JSONArray getBlogs(int from, int to, int tag)
			throws DatastoreException, NetworkException
	{
		return(read(EntityType.blog, from, to, tag));
	}

	public static JSONObject getComment(int id) throws DatastoreException,
			NetworkException
	{
		return(read(EntityType.comment, id));
	}

	public static JSONArray getComments(int from, int to, int tag)
			throws DatastoreException, NetworkException
	{
		return(read(EntityType.comment, from, to, tag));
	}
}
