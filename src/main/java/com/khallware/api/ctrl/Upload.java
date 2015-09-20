// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.ctrl;

import com.khallware.api.Datastore;
import com.khallware.api.EventFactory;
import com.khallware.api.ContactFactory;
import com.khallware.api.LocationFactory;
import com.khallware.api.DatastoreException;
import com.khallware.api.domain.Bookmark;
import com.khallware.api.domain.Sound;
import com.khallware.api.domain.Video;
import com.khallware.api.domain.Photo;
import com.khallware.api.domain.Event;
import com.khallware.api.domain.Contact;
import com.khallware.api.domain.FileItem;
import com.khallware.api.domain.Location;
import com.khallware.api.domain.APIEntity;
import com.khallware.api.domain.Credentials;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.Part;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.ServletException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map;
import java.util.List;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/upload")
@MultipartConfig(
	fileSizeThreshold = 0,                  // 0 MB 
	maxFileSize = (1024 * 1024 * 50),       // 50 MB
	maxRequestSize = (1024 * 1024 * 100))   // 100 MB
public class Upload extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static final String PROP_UPLOAD_DIR = "upload.dir";
	private static final Logger logger = LoggerFactory.getLogger(
		Upload.class);
	private static final Object semaphore = new Object();

	public void doPost(HttpServletRequest request,
			HttpServletResponse retval)
			throws ServletException, IOException
	{
		List<String> errors = new ArrayList<>();
		retval.addHeader("Content-Type", MediaType.APPLICATION_JSON);
		try {
			Util.enforceSecurity(request);
			String p = request.getServletContext().getRealPath("");
			Credentials creds = Util.getCredentials(request);
			int tagId = getIntParameter("tagId", request);
			Map<String, String> map = new HashMap<>();
			List<Part> dataParts = new ArrayList<>();
			File dir = new File(
				Datastore.DS().getProperty(PROP_UPLOAD_DIR,p));
			Util.enforceWrite(Datastore.DS().getTag(tagId), creds);
			Response rsp = null;
			dir = addScalablePath(dir);

			for (Part part : request.getParts()) {
				if (isSupplemental(part)) {
					String value = new String(
						Util.fileContentAsBytes(
							part.getInputStream()));
					logger.trace("handling \"{}\"",
						part.getName());
					map.put(part.getName(), value);
				}
				else {
					dataParts.add(part);
				}
			}
			if (map.containsKey("path")) {
				dir = new File(map.get("path"));
			}
			if (!dir.exists()) {
				dir.mkdirs();
				logger.info("created upload dir \"{}\"",""+dir);
			}
			for (Part part : dataParts) {
				logger.trace("handling \"{}\"", part.getName());
				rsp = handlePost(map, part, creds, tagId, dir);

				if (rsp != null && rsp.getStatus() != 200) {
					errors.add(""+rsp.getEntity());
				}
			}
			if (errors.size() == 0) {
				retval.setStatus(200);
				retval.getWriter().printf("%s", (rsp != null)
					? ""+rsp.getEntity()
					: "{}");
			}
			else {
				retval.setStatus(400);
				retval.getWriter().printf("{\"errors\":%s}",
					""+errors);
			}
		}
		catch (Exception e1) {
			logger.trace(""+e1, e1);
			logger.warn(""+e1);
			try {
				retval.setStatus(400);
				retval.addHeader("Content-Type",
					MediaType.APPLICATION_JSON);
				retval.getWriter().printf("{\"errors\":\"%s\"}",
					""+e1);
			}
			catch (Exception e2) {
				logger.trace(""+e2, e2);
				logger.warn(""+e2);
			}
		}
	}

	public static int getIntParameter(String parm, HttpServletRequest req)
	{
		int retval = -1;
		try {
			retval = Integer.parseInt(req.getParameter(parm));
		}
		catch (Exception e) {
			logger.trace(""+e, e);
			logger.warn(""+e);
		}
		return(retval);
	}

	protected File save(Part part, File dir) throws IOException,
			DatastoreException
	{
		return(save(part, dir, null));
	}

	protected File save(Part part, File dir, Credentials creds)
			throws IOException, DatastoreException
	{
		File retval = null;
		Datastore dstore = Datastore.DS();
		String fname = Util.sanitize(part.getSubmittedFileName());
		synchronize (semaphore) {
			long available = (creds == null)
				? Long.MAX_VALUE
				: dstore.getAvailableQuota(creds);

			if (part.getSize() > available) {
				throw new IOException("quota would be exceeded "
					+"("+available+"bytes available)");
			}
			else if (creds != null) {
				dstore.consumeQuota(creds, part.getSize());
			}
		}
		fname = (!fname.isEmpty()) ? fname : generateFilename(part);
		part.write(""+(retval = new File(""+dir, fname)));
		return(retval);
	}

	protected String[] saveReadDelete(Part part, File dir)
			throws IOException, DatastoreException
	{
		String[] retval = new String[2];
		File file = save(part, dir);
		retval[0] = ""+file;
		retval[1] = new String(Files.readAllBytes(file.toPath()));
		file.delete();
		return(retval);
	}

	protected void setMask(APIEntity entity, String mask)
	{
		try {
			if (mask != null) {
				entity.setMask(Integer.parseInt(mask));
			}
		}
		catch (NumberFormatException e) {
			logger.trace(""+e,e);
			logger.warn(""+e);
		}
	}

	protected Response handlePost(Map<String, String> map, Part part,
			Credentials creds, int tagId, File dir) throws Exception
	{
		Response retval = null;
		String[] rslt = null;
		String content = null;
		String mime = null;
		String fname = Util.sanitize(part.getSubmittedFileName());
		String name = (!map.containsKey("name"))
			? "uploaded by "+creds.getUsername()+" ("+fname+")"
			: map.get("name");
		String desc = (!map.containsKey("desc"))
			? "uploaded by "+creds.getUsername()+" ("+fname+")"
			: map.get("desc");
		CrudController ctrl = null;
		File file = null;

		switch ((mime = part.getContentType().toLowerCase())) {
		case MediaType.TEXT_PLAIN:
			InputStream is = part.getInputStream();
			BufferedReader br = new BufferedReader(
				new InputStreamReader(is));
			ctrl = new Bookmarks();

			for (String line; (line = br.readLine()) != null;) {
				Bookmark bookmark = new Bookmark();
				bookmark.setName(name);
				bookmark.setURL(line);
				setMask(bookmark, map.get("mask"));
				retval = handlePost(ctrl,bookmark,creds,tagId);
			}
			is.close();
			break;
		case "text/vcard":
			rslt = saveReadDelete(part, dir);
			ctrl = new Contacts();
			content = rslt[1];

			for (Contact contact : ContactFactory.make(content)) {
				contact.setName(name);
				setMask(contact, map.get("mask"));
				retval = handlePost(ctrl,contact,creds,tagId);
			}
			break;
		case "text/calendar":
			rslt = saveReadDelete(part, dir);
			ctrl = new Events();
			content = rslt[1];

			for (Event event : EventFactory.make(content)) {
				event.setName(name);
				setMask(event, map.get("mask"));
				retval = handlePost(ctrl, event, creds, tagId);
			}
			break;
		case "application/vnd.google-earth.kml+xml":
			file = save(part, dir);
			ctrl = new Locations();

			for (Location location : LocationFactory.make(
					new FileInputStream(file))) {
				setMask(location, map.get("mask"));
				logger.trace("post location ({})", location);
				retval = handlePost(ctrl,location,creds,tagId);
			}
			file.delete();
			break;
		case "image/jpeg":
			file = save(part, dir, creds);
			Photo photo = Photo.builder()
				.desc(desc)
				.file(file)
				.name(name)
				.build();
			setMask(photo, map.get("mask"));
			retval = handlePost(new Photos(), photo, creds, tagId);
			break;
		case "application/ogg":
			file = save(part, dir, creds);
			Sound sound = Sound.builder()
				.desc(desc)
				.file(file)
				.name(name)
				.build();
			setMask(sound, map.get("mask"));
			retval = handlePost(new Sounds(), sound, creds, tagId);
			break;
		case "video/mp4":
			file = save(part, dir, creds);
			Video video = Video.builder()
				.desc(desc)
				.file(file)
				.name(name)
				.build();
			setMask(video, map.get("mask"));
			retval = handlePost(new Videos(), video, creds, tagId);
			break;
		case "image/png":
		case "image/gif":
		case "video/avi":
		case "application/msword":
			throw new RuntimeException("files of type \""+mime+"\""
				+" are not allowed, please convert");
		default:
			file = save(part, dir, creds);
			FileItem fileItem = FileItem.builder()
				.desc(desc)
				.file(file)
				.mime(mime)
				.name(name)
				.build();
			setMask(fileItem, map.get("mask"));
			retval = handlePost(new FileItems(), fileItem, creds,
				tagId);
		}
		logger.trace("uploaded {}bytes of mime-type \"{}\"",
			part.getSize(), mime);
		return(retval);
	}

	protected Response handlePost(CrudController ctrl, APIEntity entity,
			Credentials creds, int tagId)
	{
		return(ctrl.handlePost(creds, entity, tagId));
	}

	private static boolean isSupplemental(Part part)
	{
		boolean retval = false;
		String name = (part == null) ? "" : part.getName();

		retval |= (part != null && part.getContentType() == null);
		retval |= ("name".toLowerCase().equals(name));
		retval |= ("path".toLowerCase().equals(name));
		// do not include "image", it's the main part!
		retval |= ("filename".toLowerCase().equals(name));
		retval |= ("filecomment".toLowerCase().equals(name));
		return(retval);
	}

	private static File addScalablePath(File dir)
	{
		File retval = dir;
		Date now = new Date();
		retval = new File(retval,
			new SimpleDateFormat("yyyy").format(now).toString());
		retval = new File(retval,
			new SimpleDateFormat("DDD").format(now).toString());
		retval = new File(retval,
			new SimpleDateFormat("HH").format(now).toString());
		retval = new File(retval,
			new SimpleDateFormat("mm").format(now).toString());
		return(retval);
	}

	private static String generateFilename(Part part)
	{
		StringBuilder retval = new StringBuilder();
		String fname = (""+UUID.randomUUID()).substring(0,12);
		String ext = ".dat";

		switch (part.getContentType().toLowerCase()) {
		case "application/vnd.google-earth.kml+xml":
			ext = ".kml";
			break;
		case "image/jpeg":
			ext = ".jpg";
			break;
		case "application/pdf":
			ext = ".pdf";
			break;
		case "application/ogg":
			ext = ".ogg";
			break;
		case "video/mp4":
			ext = ".mp4";
			break;
		}
		retval.append(fname).append(".").append(ext);
		return(""+retval);
	}
}
