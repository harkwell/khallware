// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.ctrl;

import com.khallware.api.enums.Mode;
import com.khallware.api.domain.Group;
import com.khallware.api.domain.Entity;
import com.khallware.api.domain.Session;
import com.khallware.api.domain.APIEntity;
import com.khallware.api.domain.AtomEntity;
import com.khallware.api.domain.Credentials;
import com.khallware.api.DatastoreException;
import com.khallware.api.dstore.Pagination;
import com.khallware.api.Datastore;
import com.khallware.api.Unauthorized;
import com.khallware.api.APIException;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.Message;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.ws.rs.core.Response;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.util.List;
import java.util.Date;
import java.util.UUID;
import java.util.Base64;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Consumer;
import java.awt.image.BufferedImage;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility functions
 *
 * @author khall
 */
public final class Util
{
	private static final Logger logger = LoggerFactory.getLogger(
		Util.class);
	public static final boolean DEF_CACHE_THUMB = true;
	public static final boolean DEF_UPDATE_SESS = true;
	public static final String PROP_EXPIRATION = "atom_timeout";
	public static final String PROP_REGIURL = "registration_url";
	public static final String PROP_ADMIN_EMAIL = "admin_email";
	public static final String PROP_FROM = "registration_from";
	public static final String PROP_SMTP_USER = "smtp_username";
	public static final String PROP_SMTP_PASS = "smtp_password";
	public static final String DEF_FROM = "system@fakedomain.com";
	public static final String DEF_ADMIN_EMAIL = "admin@fakedomain.com";
	public static final String DEF_REGIURL =
		"localhost:8080/apis/v1/security/register/";
	public static final long DEF_EXPIRATION = (5 * 24 * 60 * 60 * 1000);
	public static final int DEF_MAX_WID = 200;
	public static final int DEF_MAX_HGT = 200;

	public static Response failRequest(Throwable throwable)
	{
		Response retval = null;

		if (throwable instanceof Unauthorized) {
			retval = Response.status(401)
				.header("WWW-Authenticate",
					"Basic realm=\"khallware\"")
				.entity(""+throwable)
				.build();
		}
		else {
			retval = Response.status(400).entity(
				""+throwable).build();
		}
		return(retval);
	}

	/**
	 * Filter and order the given list to include only non expired items
	 * against the default expiration period.
	 *
	 * @param items list of items to filter
	 * @param base the base part of the URL of this servlet
	 * @param sb the collection of atoms represented by their XML equivalent
	 */
	public static List<AtomEntity> lastPeriod(List<?> items, String base,
			StringBuilder sb)
	{
		long timeout = DEF_EXPIRATION;
		String val = Datastore.DS().getProperty(PROP_EXPIRATION,
			""+DEF_EXPIRATION);
		try {
			timeout = Long.parseLong(""+val);
		}
		catch (NumberFormatException e) {
			logger.error(""+e, e);
		}
		return(lastPeriod(items, base, sb,
			new Date(new Date().getTime() - timeout)));
	}

	/**
	 * Filter the given list to include only non expired items.
	 *
	 * @param items list of items to filter
	 * @param base the base part of the URL of this servlet
	 * @param sb the collection of atoms represented by their XML equivalent
	 * @param expiration the expiration date to enforce
	 */
	public static List<AtomEntity> lastPeriod(List<?> items, String base,
			StringBuilder sb, Date expiration)
	{
		List<AtomEntity> retval = new ArrayList<>();
		List<AtomEntity> ordered = orderMax(items, 5);

		for (AtomEntity atom : ordered) {
			try {
				if (expiration.before(atom.getModified())) {
					logger.trace("including atom: {}",atom);
					logger.trace("expires: {}", expiration);
					atom.updateUrl(base);
					retval.add(atom);

					if (sb != null) {
						sb.append(atom.toXML());
					}
				}
			}
			catch (APIException e) {
				logger.error(""+e, e);
			}
		}
		return(retval);
	}

	public static List<AtomEntity> orderMax(List<?> items, int max)
	{
		List<AtomEntity> retval = new ArrayList<>();

		for (Object obj : items) {
			retval.add((AtomEntity)obj);
		}
		Collections.sort(retval, (AtomEntity e1, AtomEntity e2) ->
			e1.getModified().compareTo(e2.getModified()));
		return(retval.subList(0, Math.min(max, retval.size())));
	}

	public static String getSessionName(HttpServletRequest request)
	{
		String retval = "";
		try {
			Cookie[] cookies = request.getCookies();
			cookies = (cookies == null) ? new Cookie[] {} : cookies;

			for (Cookie cookie : cookies) {
				logger.trace("cookie \"{}\"", cookie.getName());

				if (Security.COOKIE.equals(cookie.getName())) {
					retval = cookie.getValue();
					break;
				}
			}
		}
		catch (Exception e) {
			logger.trace(""+e, e);
			logger.warn("{}",e);
		}
		return(retval);
	}

	public static void enforceSecurity(Credentials creds)
			throws Unauthorized, DatastoreException
	{
		if (creds == null) {
			throw new Unauthorized("no credentials specified");
		}
		String username = creds.getUsername();
		Credentials found = Datastore.DS().getCredentials(username);
		logger.debug("checking credentials ({})", creds);

		if (!creds.equals(found)) {
			throw new Unauthorized("authentication failed for user "
				+"\""+creds.getUsername()+"\"");
		}
		else if (found.isDisabled()) {
			throw new Unauthorized("The user account is disabled.");
		}
		logger.trace("credentials check passes");
	}

	public static void enforceSecurity(HttpServletRequest request,
			Group group) throws APIException, DatastoreException
	{
		boolean pass = false;
		Credentials creds = getCredentials(request);
		enforceSecurity(request);
		pass |= (creds.getGroup().equals(group));
		pass |= Datastore.DS().userInGroup(creds, group);

		if (!pass) {
			throw new Unauthorized("user not in group ("+group+")");
		}
	}

	public static void enforceSecurity(HttpServletRequest request)
			throws Unauthorized
	{
		Exception exception = null;
		try {
			try {
				enforceBasicAuth(request);
			}
			catch (APIException e) {
				String msg = "no existing session cookie or "+e;
				exception = (enforceValidOrNewSession(request))
					? new Unauthorized(msg)
					: null;
			}
		}
		catch (APIException|DatastoreException e) {
			exception = e;
		}
		if (exception != null) {
			if (exception instanceof Unauthorized) {
				throw (Unauthorized)exception;
			}
			else {
				throw new Unauthorized(exception);
			}
		}
		logger.trace("security check passes");
	}

	public static Session getSession(HttpServletRequest request)
	{
		Session retval = null;
		try {
			String sessName = getSessionName(request);
			logger.trace("session name \"{}\"", sessName);
			retval = Datastore.DS().getSession(sessName);
		}
		catch (DatastoreException e) {
			logger.error(""+e, e);
		}
		return(retval);
	}

	/**
	 * Throws an exception if the cookie specified session is not valid,
	 * otherwise return whether this is a new session or not.
	 */
	public static boolean enforceValidOrNewSession(
			HttpServletRequest request) throws APIException,
			DatastoreException
	{
		logger.trace("enforceValidOrNewSession()...");
		return(enforceValidOrNewSession(request,
			(Session s) -> {
				try {
					logger.trace("saving session ({})", s);
					Datastore.DS().save(s);
				}
				catch (DatastoreException e) {
					logger.error(""+e, e);
				}
			},
			(Credentials c) -> {
				try {
					createNewSession(c);
				}
				catch (DatastoreException e) {
					logger.error(""+e, e);
				}
			}));
	}

	/**
	 * If this session is current, call consumer1, if it is new call
	 * consumer2.
	 */
	protected static boolean enforceValidOrNewSession(
			HttpServletRequest request, Consumer<Session> consumer1,
			Consumer<Credentials> consumer2) throws APIException,
			DatastoreException
	{
		boolean retval = false;
		Session session = null;
		Credentials creds = null;
		boolean newSession = ((session = getSession(request)) == null);

		if (!newSession) {
			if (!session.isValid()) {
				throw new Unauthorized("session expired");
			}
			consumer1.accept(session);
		}
		else if ((creds = getCredentials(request)) == null) {
			throw new DatastoreException("no credentials exist for "
				+"this request or cookie or session not found "
				+"\""+getSessionName(request)+"\"");
		}
		else {
			retval = true;
			consumer2.accept(creds);
		}
		logger.debug("session ({})", session);
		logger.debug("credentials ({})", creds);
		logger.trace("session check passes");
		logger.trace("this is "+((retval)?"":"not ")+"a new session");
		return(retval);
	}

	public static Session createNewSession(String uname)
			throws DatastoreException
	{
		return(createNewSession(Datastore.DS().getCredentials(uname)));
	}

	public static Session createNewSession(Credentials creds)
			throws DatastoreException
	{
		Session retval = new Session();
		retval.setCredentials(Datastore.DS().getCredentials(creds));
		retval.setName(""+UUID.randomUUID());
		Datastore.DS().save(retval);
		logger.debug("created session ({})", retval);
		return(retval);
	}

	public static void enforceBasicAuth(HttpServletRequest request)
			throws APIException, DatastoreException
	{
		Session session = null;
		enforceSecurity(getCredentials(request, false));
		logger.trace("basic authentication check passes");

		if ((session = getSession(request)) != null) {
			Datastore.DS().save(session);
			logger.trace("session updated");
		}
	}

	public static APIEntity enforceRead(APIEntity entity, Credentials creds)
			throws Unauthorized, DatastoreException
	{
		logger.trace("creds ({}), entity ({})", creds, entity);
		enforceUGOModes(creds, entity, Mode.userRead, Mode.groupRead,
			Mode.otherRead);
		return(entity);
	}

	public static APIEntity enforceWrite(APIEntity entity,Credentials creds)
			throws Unauthorized, DatastoreException
	{
		logger.trace("creds ({}), entity ({})", creds, entity);
		enforceUGOModes(creds, entity, Mode.userWrite, Mode.groupWrite,
			Mode.otherWrite);
		return(entity);
	}

	public static APIEntity enforceExec(APIEntity entity, Credentials creds)
			throws Unauthorized, DatastoreException
	{
		logger.trace("creds ({}), entity ({})", creds, entity);
		enforceUGOModes(creds, entity, Mode.userExec, Mode.groupExec,
			Mode.otherExec);
		return(entity);
	}

	protected static APIEntity enforceUGOModes(Credentials creds,
			APIEntity entity, Mode m1, Mode m2, Mode m3)
			throws Unauthorized, DatastoreException
	{
		if (entity == null) {
			return(entity);
		}
		boolean auth = false;
		boolean isOwner = (creds.getId() == entity.getUser());
		boolean isGroup = (creds.getGroup().equals(entity.getGroup()));
		boolean isWorld = true;
		int mask = entity.getMask();
		isGroup |= Datastore.DS().userInGroup(creds, entity.getGroup());

		auth |= (isOwner && Mode.matches(m1, mask));
		auth |= (isGroup && Mode.matches(m2, mask));
		auth |= (isWorld && Mode.matches(m3, mask));

		if (!auth) {
			logger.trace("I am "+((isOwner) ?"" :"not ")+"owner");
			logger.trace("I am "+((isGroup) ?"" :"not ")+"group");
			logger.trace("I am "+((isWorld) ?"" :"not ")+"world");
			logger.trace(""+m1+": "+Mode.matches(m1, mask));
			logger.trace(""+m2+": "+Mode.matches(m2, mask));
			logger.trace(""+m3+": "+Mode.matches(m3, mask));
			throw new Unauthorized("denied for mask="+mask);
		}
		return(entity);
	}

	/**
	 * Retrieve the user Credentials from the session or create from
	 * Authorization header if either exists, otherwise return null.
	 */
	public static Credentials getCredentials(HttpServletRequest request)
			throws APIException
	{
		return(getCredentials(request, true));
	}

	protected static Credentials getCredentials(HttpServletRequest request,
			boolean inspectCookie) throws APIException
	{
		Credentials retval = null;
		String header = request.getHeader("Authorization");
		String decoded = null;
		String[] tokens = null;
		logger.debug("header ({})", header);

		if (header == null && inspectCookie) {
			Session session = getSession(request);
			logger.debug("session \"{}\"", session);
			retval = (session != null)
				? session.getCredentials()
				: null;
		}
		else if (header != null) {
			decoded = base64decode(
				header.replaceAll("Basic\\s+",""));

			if ((tokens = decoded.split(":")).length != 2) {
				throw new APIException("bad auth header format "
					+"\""+decoded+"\"");
			}
			try {
				String passwd = hash(tokens[1]);
				Credentials pattern = new Credentials(
					tokens[0], passwd, null);
				retval = Datastore.DS().getCredentials(pattern);
				retval = (retval == null) ? pattern : retval;
				retval.setPassword(passwd);
			}
			catch (DatastoreException|NoSuchAlgorithmException e) {
				throw new APIException(e);
			}
		}
		return(retval);
	}

	protected static String hash(String plain) throws APIException,
			NoSuchAlgorithmException
	{
		return(com.khallware.api.Util.hash(plain));
	}

	public static String base64decode(String encoded) throws APIException
	{
		byte[] rslt = null;
		encoded = (encoded.endsWith("="))
			? encoded.substring(0, encoded.length() -1)
			: encoded;
		try {
			rslt = Base64.getDecoder().decode(encoded);
		}
		catch (IllegalArgumentException e) {
			throw new APIException("not base64 encoded", e);
		}
		return(new String(rslt));
	}

	@Deprecated
	public static <T extends APIEntity> List<T> authFilter(
			HttpServletRequest request, List<T> list)
	{
		Credentials creds = null;
		try {
			Credentials pattern = getCredentials(request);
			creds = (pattern == null)
				? makeAnonymousCreds()
				: Datastore.DS().getCredentials(pattern);
		}
		catch (DatastoreException|APIException e) {
			logger.error(""+e, e);
		}
		return(Util.<T>authFilter(creds, list));
	}

	@Deprecated
	public static <T extends APIEntity> List<T> authFilter(
			Credentials creds, List<T> retval)
	{
		List<APIEntity> copy = new ArrayList<>();
		String user = creds.getUsername();
		Group group = creds.getGroup();
		logger.debug("filtering {} items", retval.size());
		copy.addAll(retval);
		retval.clear();

		for (Object obj : copy) {
			APIEntity entity = null;
			try {
				entity = (APIEntity)obj;
				enforceRead(entity, creds);
				retval.add((T)entity);
			}
			catch (Unauthorized|DatastoreException e) {
				logger.trace("\"{}\" prohibited to read entity "
					+entity.getId(), user);
			}
			catch (ClassCastException e) {
				continue;
			}
		}
		logger.debug("results in {} items", retval.size());
		return(retval);
	}

	public static Credentials makeAnonymousCreds()
	{
		Group nogroup = new Group("nogroup", "nogroup");
		return(new Credentials("anonymous", "", nogroup));
	}

	public static byte[] makeThumbnail(File file, int id, String propKey,
			String def) throws IOException
	{
		String thumb = String.format("%09d-thumb.jpg", id);
		return(makeThumbnail(file, resolveFile(thumb, propKey, def)));
	}

	public static byte[] makeThumbnail(File file, File thumb)
			throws IOException
	{
		byte[] retval = (thumb.exists())
			? fileContentAsBytes(thumb)
			: makeThumbnail(fileContentAsBytes(file));
		boolean storeCache = Datastore.DS().getProperties().containsKey(
			Photos.PROP_REPOTHUMBS);
		logger.debug("thumbnail ({})", ""+thumb);

		if (!thumb.exists() && storeCache) {
			FileOutputStream os = new FileOutputStream(thumb);
			os.write(retval);
			os.close();
		}
		return(retval);
	}

	public static byte[] makeThumbnail(byte[] image) throws IOException
	{
		return(makeThumbnail(makeImage(image),DEF_MAX_WID,DEF_MAX_HGT));
	}

	public static byte[] makeThumbnail(BufferedImage image, int width,
			int height) throws IOException
	{
		return(toBytes(Scalr.resize(image, Math.max(width, height))));
	}

	public static BufferedImage scale(BufferedImage image, int width,
			int height)
	{
		BufferedImage retval = new BufferedImage(width, height,
			BufferedImage.TYPE_INT_RGB);
		final Graphics2D gfx2D = retval.createGraphics();
		gfx2D.setComposite(AlphaComposite.Src);
		gfx2D.drawImage(image, 0, 0, width, height, null);
		gfx2D.dispose();
		return(retval);
	}

	public static BufferedImage makeImage(byte[] bytes) throws IOException
	{
		BufferedImage retval = null;
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		retval = ImageIO.read(new MemoryCacheImageInputStream(is));
		return(retval);
	}

	public static byte[] toBytes(BufferedImage image) throws IOException
	{
		ByteArrayOutputStream retval = new ByteArrayOutputStream();
		ImageWriter writer = getImageWriter("JPG", 0.95f);
		writer.setOutput(new MemoryCacheImageOutputStream(retval));
		writer.write(image);
		return(retval.toByteArray());
	}

	public static ImageWriter getImageWriter(String format, float quality)
	{
		ImageWriter retval = null;
		Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName(
			format);

		if (it.hasNext()) {
			retval = it.next();
		}
		return(retval);
	}

	@Deprecated
	public static <T extends AtomEntity> List<T> sortAndPaginate(
			final List<T> retval, int from, int size)
	{
		List<AtomEntity> rslt = orderMax(retval, retval.size());
		logger.trace("paginating list of size {}", retval.size());
		int to = (from + size);
		retval.clear();
		from = Math.max(from, 0);
		to = Math.min(to, rslt.size());
		to = (to < 0) ? rslt.size() : to;
		from = Math.min(to, from);
		logger.trace("paginate from {} to {}", from, to);

		for (AtomEntity entity : rslt.subList(from, to)) {
			try {
				retval.add((T)entity);
			}
			catch (ClassCastException e) {
				logger.error(""+e, e);
			}
		}
		return(retval);
	}

	public static void sendRegistrationEmail(Credentials creds)
	{
		StringBuilder sb = new StringBuilder();
		String regiURL = Datastore.DS().getProperty(
			PROP_REGIURL, DEF_REGIURL);
		logger.debug("sending registration email...");
		sb.append("visit the following URL to activate the account:\n");
		sb.append(regiURL+creds.getRegikey()+".html\n");
		try {
			sendEmail(creds.getEmail(),
				"your khallware registration",""+sb);
		}
		catch (Exception e) {
			logger.error(""+e, e);
		}
	}

	public static void sendNewActiveUserNotification(Credentials creds)
	{
		StringBuilder sb = new StringBuilder();
		String email = Datastore.DS().getProperty(
			PROP_ADMIN_EMAIL, DEF_ADMIN_EMAIL);
		logger.debug("sending new user notifcation email...");
		sb.append("A new user has been registered and activated:\n");
		sb.append("Username: "+creds.getUsername()+"\n");
		sb.append("Email: "+creds.getEmail()+"\n");
		try {
			sendEmail(email, "new khallware user",""+sb);
		}
		catch (Exception e) {
			logger.error(""+e, e);
		}
	}

	public static void sendPasswordChangeEmail(Credentials creds)
	{
		StringBuilder sb = new StringBuilder();
		logger.debug("sending password change email...");
		sb.append("Your khallware password has been changed.\n");
		try {
			sendEmail(creds.getEmail(),
				"your khallware password",""+sb);
		}
		catch (Exception e) {
			logger.error(""+e, e);
		}
	}

	public static void sendEmail(String email, String subject, String body)
			throws MessagingException
	{
		Properties props = Datastore.DS().getProperties();
		javax.mail.Session session = javax.mail.Session.getInstance(
			props);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(props.getProperty(PROP_FROM, DEF_FROM));
		message.setRecipients(Message.RecipientType.TO, email);
		message.setSubject(subject);
		message.setText(body);

		if (props.getProperty("mail.transport.protocol","").isEmpty()) {
			Transport.send(message);
		}
		else {
			Transport transport = session.getTransport();
			String host = props.getProperty("mail.smtp.host");
			String user = props.getProperty(PROP_SMTP_USER);
			String pass = props.getProperty(PROP_SMTP_PASS);
			transport.connect(host, user, pass);
			transport.sendMessage(
				message, message.getAllRecipients());
		}
	}

	public static File resolveFile(File file)
	{
		File retval = file;
		String[] repoDirs = new String[] {
			Photos.PROP_REPODIR, FileItems.PROP_REPODIR,
			Sounds.PROP_REPODIR, Videos.PROP_REPODIR,
			Photos.PROP_REPOTHUMBS
		};
		for (String repoDir : repoDirs) {
			retval = resolveFile(file, repoDir);

			if (retval.exists()) {
				break;
			}
		}
		return(retval);
	}

	public static File resolveFile(File file, String propKey)
	{
		return(resolveFile(file, propKey, "/tmp"));
	}

	public static File resolveFile(String file, String propKey, String def)
	{
		return(resolveFile(new File(file), propKey, def));
	}

	public static File resolveFile(File file, String propKey, String def)
	{
		File retval = file;
		String propVal = Datastore.DS().getProperty(propKey, def);

		if (!retval.exists()) {
			retval = new File(propVal, ""+file);
		}
		return(retval);
	}

	public static byte[] fileContentAsBytes(File file) throws IOException
	{
		return(fileContentAsBytes(new FileInputStream(file)));
	}

	public static byte[] fileContentAsBytes(InputStream is)
			throws IOException
	{
		ByteArrayOutputStream retval = new ByteArrayOutputStream();

		for (int ch; (ch=is.read()) != -1;) {
			retval.write(ch);
		}
		is.close();
		retval.close();
		return(retval.toByteArray());
	}

	public static String sanitize(String fname)
	{
		String retval = (fname == null) ? "" : fname;
		int flags = Pattern.CASE_INSENSITIVE;
		String[] regexArray = new String[] {
			"\\.\\.", File.pathSeparator, " ", ";"
		};
		for (String regex : regexArray) {
			Pattern pattern = Pattern.compile(regex, flags);
			Matcher matcher = pattern.matcher(retval);

			if (matcher.matches()) {
				matcher.replaceAll("");
			}
		}
		return(retval);
	}
}
