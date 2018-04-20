// Copyright Kevin D.Hall 2014-2018

package com.khallware.api;

import com.khallware.api.dstore.Operator.Operation;
import com.khallware.api.dstore.APICrudChain;
import com.khallware.api.dstore.OrphanUtil;
import com.khallware.api.dstore.DBookmarks;
import com.khallware.api.dstore.DBlogs;
import com.khallware.api.dstore.DEvents;
import com.khallware.api.dstore.DComments;
import com.khallware.api.dstore.DContacts;
import com.khallware.api.dstore.DFileItems;
import com.khallware.api.dstore.DLocations;
import com.khallware.api.dstore.DSessions;
import com.khallware.api.dstore.DPhotos;
import com.khallware.api.dstore.DSounds;
import com.khallware.api.dstore.DVideos;
import com.khallware.api.dstore.DTags;
import com.khallware.api.dstore.CrudChain;
import com.khallware.api.dstore.Pagination;
import com.khallware.api.dstore.Operator;
import com.khallware.api.domain.Entity;
import com.khallware.api.domain.Tag;
import com.khallware.api.domain.Blog;
import com.khallware.api.domain.Edge;
import com.khallware.api.domain.Event;
import com.khallware.api.domain.Photo;
import com.khallware.api.domain.Group;
import com.khallware.api.domain.Video;
import com.khallware.api.domain.Sound;
import com.khallware.api.domain.Contact;
import com.khallware.api.domain.Comment;
import com.khallware.api.domain.Session;
import com.khallware.api.domain.FileItem;
import com.khallware.api.domain.Location;
import com.khallware.api.domain.BlogTags;
import com.khallware.api.domain.Bookmark;
import com.khallware.api.domain.EventTags;
import com.khallware.api.domain.PhotoTags;
import com.khallware.api.domain.APIEntity;
import com.khallware.api.domain.VideoTags;
import com.khallware.api.domain.SoundTags;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.ContactTags;
import com.khallware.api.domain.BookmarkTags;
import com.khallware.api.domain.FileItemTags;
import com.khallware.api.domain.LocationTags;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.db.MysqlDatabaseType;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.DeleteBuilder;
import java.text.SimpleDateFormat;
import java.sql.SQLException;
import java.util.UUID;
import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Properties;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * The Datastore singleton. All database access is accomplished via this
 * class.  It is accessed as a singleton pattern.
 *
 * @author khall
 */
public final class Datastore
{
	private static final Logger logger = LoggerFactory.getLogger(
		Datastore.class);
	public static final int DEF_POOLSIZE = 150; // 1 less than mysql def max
	public static final String PROP_DBURL = "jdbc_url";
	public static final String PROP_DBUSER = "jdbc_user";
	public static final String PROP_DBPASS = "jdbc_pass";
	public static final String PROP_DBDRIVER = "jdbc_driver";
	public static final String DEF_DRIVER = "com.mysql.jdbc.Driver";
	public static final String DEF_DBURL = "jdbc:mysql://localhost/website";
	public static final String DEF_DBUSER = "api";
	public static final String DEF_DBPASS = "api";
	public static final int MAX_LIST_SIZE = 500;

	private static Datastore instance = null;
	private static ComboPooledDataSource cpds = null;
	private static String username = DEF_DBUSER;
	private static String password = DEF_DBPASS;
	private static String url = DEF_DBURL;
	private static Properties props = null;
	private static CrudChain chain = null;

	/**
	 * A class when declared final can be used to assign and access its
	 * internal variable.
	 */
	public static class Wrapper<T>
	{
		public long count = -1;
		public T item;
	}

	private Datastore() {}

	/**
	 * A static method to access the singleton instance.
	 */
	public static Datastore DS()
	{
		if (instance == null) {
			instance = new Datastore();
			chain = new DTags();
			chain.next(new DLocations())
				.next(new DBlogs())
				.next(new DComments())
				.next(new DContacts())
				.next(new DEvents())
				.next(new DFileItems())
				.next(new DPhotos())
				.next(new DSessions())
				.next(new DSounds())
				.next(new DBookmarks())
				.next(new DVideos());
		}
		return(instance);
	}

	/**
	 * Set the properties.
	 *
	 * @param props - The Properties to set.
	 */
	public static void configure(Properties props)
	{
		Datastore.props = props;
		url = props.getProperty(PROP_DBURL, url);
		username = props.getProperty(PROP_DBUSER, username);
		password = props.getProperty(PROP_DBPASS, password);
	}

	/**
	 * Get the properties.
	 *
	 * @return Properties - The Properties previously set.
	 */
	public Properties getProperties()
	{
		return(props);
	}

	/**
	 * Get a single property.
	 *
	 * @param key - The Property key.
	 * @return String - The Property value for the specified key.
	 */
	public String getProperty(String key)
	{
		return(props.getProperty(key));
	}

	/**
	 * Get a single property.
	 *
	 * @param key - The Property key.
	 * @param def - The default value for the key.
	 * @return String - The Property value for the specified key.
	 */
	public String getProperty(String key, String def)
	{
		return(props.getProperty(key, def));
	}

	public static synchronized ComboPooledDataSource getDataSource()
	{
		String driverName = DS().getProperty(PROP_DBDRIVER, DEF_DRIVER);

		if (Datastore.cpds == null) {
			try {
				Datastore.cpds = new ComboPooledDataSource();
				Datastore.cpds.setDriverClass(driverName);
				Datastore.cpds.setMaxPoolSize(DEF_POOLSIZE);
				Datastore.cpds.setJdbcUrl(url);
				Datastore.cpds.setUser(username);
				Datastore.cpds.setPassword(password);
			}
			catch (Exception e) {
				logger.error(""+e, e);
			}
		}
		return(Datastore.cpds);
	}

	public boolean ping()
	{
		boolean retval = false;
		try {
			getGroups("");
			retval = true;
		}
		catch (Exception e) {
			logger.trace(""+e, e);
		}
		return(retval);
	}

	/**
	 * Persist the entity to the database.
	 *
	 * @param entity - The Entity to persist to the database.
	 */
	public void save(Entity entity) throws DatastoreException
	{
		chain.create(entity);
	}

	/**
	 * Persist the user credentials to the database.
	 *
	 * @param creds - The Credentials to persist to the database.
	 */
	public void save(final Credentials creds) throws DatastoreException
	{
		Operator.<Credentials>perform((dao) -> {
			dao.createOrUpdate(creds);
		}, Credentials.class);
	}

	/**
	 * Persist the user group to the database.
	 *
	 * @param group - The Group to persist to the database.
	 */
	public void save(final Group group) throws DatastoreException
	{
		Operator.<Group>perform((dao) -> {
			dao.createOrUpdate(group);
		}, Group.class);
	}

	/**
	 * Persist the group edge to the database.  An edge is a traditianal
	 * graph edge list edge.
	 * http://en.wikipedia.org/wiki/Graph_%28mathematics%29
	 *
	 * @param edge - The Edge to persist to the database.
	 */
	public void save(final Edge edge) throws DatastoreException
	{
		Operator.<Edge>perform((dao) -> {
			dao.create(edge);
		}, Edge.class);
	}

	/**
	 * Retrieve the user Credentials from the database.
	 *
	 * @param edge - The Edge to persist to the database.
	 * @return Credentials - The user as credentials.
	 */
	public Credentials getCredentials(Credentials creds)
			throws DatastoreException
	{
		// use the username, because basic auth does not know id
		return(getCredentials(creds.getUsername()));
	}

	/**
	 * Retrieve the user Credentials from the database.
	 *
	 * @param user - The user as represented by its unique identifier.
	 * @return Credentials - The user as credentials.
	 */
	public Credentials getCredentials(final int user)
			throws DatastoreException
	{
		return(getCredentials(Credentials.COL_ID, ""+user));
	}

	/**
	 * Retrieve the user Credentials from the database.
	 *
	 * @param user - The user as represented by their user name.
	 * @return Credentials - The user as credentials.
	 */
	public Credentials getCredentials(final String user)
			throws DatastoreException
	{
		return(getCredentials(Credentials.COL_USER, user));
	}

	/**
	 * Retrieve the user Credentials from the database.
	 *
	 * @param user - The user as represented by their registration uuid.
	 * @return Credentials - The user as credentials.
	 */
	public Credentials getCredentials(final UUID regikey)
			throws DatastoreException
	{
		return(getCredentials(Credentials.COL_REGIKEY, ""+regikey));
	}

	/**
	 * Retrieve the user Credentials from the database.
	 *
	 * @param column - The user as represented by given column name.
	 * @param value - The user as represented by the column value.
	 * @return Credentials - The user as credentials.
	 */
	public Credentials getCredentials(final String column,
			final String value) throws DatastoreException
	{
		final Wrapper<Credentials> retval = new Wrapper<>();
		Operator.<Credentials>perform((dao) -> {
			for (Credentials creds : dao.queryForEq(column,value)) {
				retval.item = creds;
				break; // get first cred found or don't set
			}
		}, Credentials.class);
		return(retval.item);
	}

	/**
	 * Retrieve the Tag from the database.
	 *
	 * @param id - The tag as represented by its unique identifier.
	 * @return Tag - The entity tag.
	 */
	public Tag getTag(long id) throws DatastoreException
	{
		return((Tag)chain.read(Tag.class, id));
	}

	/**
	 * Retrieve an entity from the database.
	 *
	 * @param clazz - The type for the entity.
	 * @param id - The tag as represented by its unique identifier.
	 * @return APIEntity - The entity.
	 */
	public APIEntity get(Class clazz, long id) throws DatastoreException
	{
		return((APIEntity)chain.read(clazz, id));
	}

	public Bookmark getBookmark(long id) throws DatastoreException
	{
		return((Bookmark)chain.read(Bookmark.class, id));
	}

	public Location getLocation(long id) throws DatastoreException
	{
		return((Location)chain.read(Location.class, id));
	}

	public Contact getContact(long id) throws DatastoreException
	{
		return((Contact)chain.read(Contact.class, id));
	}

	public Event getEvent(long id) throws DatastoreException
	{
		return((Event)chain.read(Event.class, id));
	}

	public Photo getPhoto(long id) throws DatastoreException
	{
		return((Photo)chain.read(Photo.class, id));
	}

	public FileItem getFileItem(long id) throws DatastoreException
	{
		return((FileItem)chain.read(FileItem.class, id));
	}

	public Sound getSound(long id) throws DatastoreException
	{
		return((Sound)chain.read(Sound.class, id));
	}

	public Video getVideo(long id) throws DatastoreException
	{
		return((Video)chain.read(Video.class, id));
	}

	public Blog getBlog(long id) throws DatastoreException
	{
		return((Blog)chain.read(Blog.class, id));
	}

	public Comment getComment(long id) throws DatastoreException
	{
		return((Comment)chain.read(Comment.class, id));
	}

	/**
	 * Retrieve a user group from the database.
	 *
	 * @param id - The group as represented by its unique identifier.
	 * @return Group - The user group.
	 */
	public Group getGroup(final long id) throws DatastoreException
	{
		final Wrapper<Group> retval = new Wrapper<>();
		Operator.<Group>perform((dao) -> {
			Group group = dao.queryForId((int)id);
			retval.item = group;

			if (group == null || group.isDisabled()) {
				retval.item = null;
			}
		}, Group.class);
		return(retval.item);
	}

	protected List<Group> getParentsOf(final Group group)
			throws DatastoreException
	{
		final List<Group> retval = new ArrayList<>();
		Operator.<Edge>perform((dao) -> {
			String col = Edge.COL_GROUP;

			for (Edge edge : dao.queryForEq(col, group)) {
				Group found = (edge.getParent() == null)
					? new Group()  // root
					: edge.getParent();

				if (!retval.contains(found)
						&& !found.isDisabled()) {
					retval.add(found);
					logger.trace("found edge ({})", edge);
				}
			}
		}, Edge.class);
		return(retval);
	}

	/**
	 * Retrieve a list of user groups (parents of) from the database.
	 *
	 * @param child - The group to retrieve all parents for.
	 * @return List<Group> - The list of user groups.
	 */
	public List<Group> getGroups(final Group child)
			throws DatastoreException
	{
		List<Group> retval = new ArrayList<>();

		if (child != null) {
			for (Group parent : getParentsOf(child)) {
				retval.add(parent);
				retval.addAll(getGroups(parent));
			}
		}
		return(retval);
	}

	/**
	 * Retrieve a list of groups that this user belongs to.
	 *
	 * @param creds - The user in question.
	 * @return List<Group> - The list of groups that belong to this user.
	 */
	public List<Group> getGroups(final Credentials creds)
			throws DatastoreException
	{
		List<Group> retval = new ArrayList<>();
		Group primary = creds.getGroup();
		logger.trace("getGroups({})", creds);
		retval.add(primary);

		for (Group group : getGroups(primary)) {
			if (group != null && !retval.contains(group)) {
				retval.add(group);
			}
		}
		return(retval);
	}

	public List<Tag> getTags(final Pagination pg, final Credentials creds)
			throws DatastoreException
	{
		return(chain.read(Tag.class, pg, creds));
	}

	/**
	 * Retrieve a list of tags that match this name that can be ready by
	 * this credential.
	 *
	 * @param pg - A Pagination instance.
	 * @param name - The pattern to match against.
	 * @param creds - The credential used for security.
	 * @return List<Tag> - The list of tags this user can see that match.
	 */
	public List<Tag> getTagsByName(final Pagination pg, final String name,
			final Credentials creds) throws DatastoreException
	{
		final Wrapper<List<Tag>> retval = new Wrapper<>();
		Operator.<Tag>perform((dao) -> {
			QueryBuilder<Tag, Integer> qb = dao.queryBuilder()
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			Where where = qb.where();
			retval.item = new ArrayList<Tag>();
			where.and(
				where.eq(Tag.COL_NAME, name),
				APICrudChain.filterOnMode(where, creds));
			retval.item.addAll(qb.query());
		}, Tag.class);
		return(retval.item);
	}

	public List<APIEntity> find(final APIEntity pattern)
			throws DatastoreException
	{
		return(chain.read(pattern));
	}

	public List<APIEntity> find(final Pagination pg,
			final Credentials creds, final APIEntity pattern)
			throws DatastoreException
	{
		return(chain.read(pg, creds, pattern));
	}

	public List<Entity> get(final Class clazz, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		return(chain.read(clazz, pg, creds));
	}

	public List<Bookmark> getBookmarks(final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		return(chain.read(Bookmark.class, pg, creds));
	}

	public List<Contact> getContacts(final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		return(chain.read(Contact.class, pg, creds));
	}

	public List<Event> getEvents(final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		return(chain.read(Event.class, pg, creds));
	}

	public List<Blog> getBlogs(final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		return(chain.read(Blog.class, pg, creds));
	}

	public List<Photo> getPhotos(final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		return(chain.read(Photo.class, pg, creds));
	}

	public List<FileItem> getFileItems(final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		return(chain.read(FileItem.class, pg, creds));
	}

	public List<Sound> getSounds(final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		return(chain.read(Sound.class, pg, creds));
	}

	public List<Video> getVideos(final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		return(chain.read(Video.class, pg, creds));
	}

	public List<Group> getGroups(String name) throws DatastoreException
	{
		return(getGroups(new Pagination(), name));
	}

	public List<Group> getGroups(final Pagination pg, final String name)
			throws DatastoreException
	{
		final Wrapper<List<Group>> retval = new Wrapper<>();
		Operator.<Group>perform((dao) -> {
			QueryBuilder<Group, Integer> qb = dao.queryBuilder()
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			retval.item = new ArrayList<Group>();
			qb.where().like(Group.COL_NAME, name);
			retval.item.addAll(qb.query());
		}, Group.class);
		return(retval.item);
	}

	public List<Group> getGroups(final Pagination pg)
			throws DatastoreException
	{
		final Wrapper<List<Group>> retval = new Wrapper<>();
		Operator.<Group>perform((dao) -> {
			QueryBuilder<Group, Integer> qb = dao.queryBuilder()
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			retval.item = new ArrayList<Group>();
			qb.where().ne(Group.COL_DISABLED, 1);
			retval.item.addAll(qb.query());
		}, Group.class);
		return(retval.item);
	}

	public List<Credentials> getCredentials(final Pagination pg)
			throws DatastoreException
	{
		return(findCredentials(pg, null));
	}

	public List<Credentials> findCredentials(final Credentials pattern)
			throws DatastoreException
	{
		return(findCredentials(new Pagination(), pattern));
	}

	public List<Credentials> findCredentials(final Pagination pg,
			final Credentials pattern) throws DatastoreException
	{
		final Wrapper<List<Credentials>> retval = new Wrapper<>();
		Operator.<Credentials>perform((dao) -> {
			QueryBuilder<Credentials, Integer> qb =
				dao.queryBuilder()
					.offset(pg.calcCursorIndex())
					.limit(pg.getPageSize());
			retval.item = new ArrayList<Credentials>();
			retval.item.addAll((pattern == null)
				? qb.query()
				: dao.queryForMatchingArgs(pattern));
		}, Credentials.class);
		return(retval.item);
	}

	public List<Session> getSessions(final Pagination pg)
			throws DatastoreException
	{
		return(chain.read(Session.class, pg, null));
	}

	/**
	 * Retrieve a list of bookmarks for the given tag.  If the tag is
	 * the root tag, include bookmarks without explicit tags.
	 */
	public List<Bookmark> getBookmarks(final Tag tag, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		final List<Bookmark> retval =
			chain.read(Bookmark.class, tag, pg, creds);
		retval.addAll(new OrphanUtil<Bookmark>() {
			public List<Bookmark> execGet(Pagination p)
					throws DatastoreException
			{
				return(getOrphanedBookmarks(p, creds));
			}
		}.getOrphans(tag, pg));
		return(retval);
	}

	/**
	 * Retrieve a list of photos for the given tag.  If the tag is
	 * the root tag, include photos without explicit tags.
	 */
	public List<Photo> getPhotos(final Tag tag, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		final List<Photo> retval = new ArrayList<>();
			chain.read(Photo.class, tag, pg, creds);
		retval.addAll(new OrphanUtil<Photo>() {
			public List<Photo> execGet(Pagination p)
					throws DatastoreException
			{
				return(getOrphanedPhotos(p, creds));
			}
		}.getOrphans(tag, pg));
		return(retval);
	}

	/**
	 * Retrieve a list of fileitems for the given tag.  If the tag is
	 * the root tag, include fileitems without explicit tags.
	 */
	public List<FileItem> getFileItems(final Tag tag, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		final List<FileItem> retval =
			chain.read(FileItem.class, tag, pg, creds);
		retval.addAll(new OrphanUtil<FileItem>() {
			public List<FileItem> execGet(Pagination p)
					throws DatastoreException
			{
				return(getOrphanedFileItems(p, creds));
			}
		}.getOrphans(tag, pg));
		return(retval);
	}

	/**
	 * Retrieve a list of sounds for the given tag.  If the tag is
	 * the root tag, include sounds without explicit tags.
	 */
	public List<Sound> getSounds(final Tag tag, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		final List<Sound> retval =
			chain.read(Sound.class, tag, pg, creds);
		retval.addAll(new OrphanUtil<Sound>() {
			public List<Sound> execGet(Pagination p)
					throws DatastoreException
			{
				return(getOrphanedSounds(p, creds));
			}
		}.getOrphans(tag, pg));
		return(retval);
	}

	public List<Video> getVideos(final Tag tag, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		final List<Video> retval =
			chain.read(Video.class, tag, pg, creds);
		retval.addAll(new OrphanUtil<Video>() {
			public List<Video> execGet(Pagination p)
					throws DatastoreException
			{
				return(getOrphanedVideos(p, creds));
			}
		}.getOrphans(tag, pg));
		return(retval);
	}

	public List<Entity> get(final Class clazz, final Tag tag,
			final Pagination pg, final Credentials creds)
			throws DatastoreException
	{
		return(chain.read(clazz, tag, pg, creds));
	}

	/**
	 * Retrieve a list of blogs for the given tag.  If the tag is
	 * the root tag, include blogs without explicit tags.
	 */
	public List<Blog> getBlogs(final Tag tag, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		final List<Blog> retval = chain.read(Blog.class,tag,pg,creds);
		retval.addAll(new OrphanUtil<Blog>() {
			public List<Blog> execGet(Pagination p)
					throws DatastoreException
			{
				return(getOrphanedBlogs(p, creds));
			}
		}.getOrphans(tag, pg));
		return(retval);
	}

	/**
	 * Retrieve a list of contacts for the given tag.  If the tag is
	 * the root tag, include contacts without explicit tags.
	 */
	public List<Contact> getContacts(final Tag tag, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		final List<Contact> retval =
			chain.read(Contact.class, tag, pg, creds);
		retval.addAll(new OrphanUtil<Contact>() {
			public List<Contact> execGet(Pagination p)
					throws DatastoreException
			{
				return(getOrphanedContacts(p, creds));
			}
		}.getOrphans(tag, pg));
		return(retval);
	}

	/**
	 * Retrieve a list of comments for the given tag.  If the tag is
	 * the root tag, include comments without explicit tags.
	 */
	public List<Comment> getComments(final Blog blog, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		return(findComments(blog, pg, creds, null));
	}

	public List<Comment> findComments(final Comment pattern)
			throws DatastoreException
	{
		// java.lang.IllegalStateException: Expecting there to be a
		//      clause already defined for 'AND' operation
		//return(findComments(pattern.getBlog(), new Pagination(), null,
		//	pattern));
		return(new ArrayList<Comment>());
	}

	public List<Comment> findComments(final Blog blog, final Pagination pg,
			final Credentials creds, final Comment pattern)
			throws DatastoreException
	{
		final List<Comment> retval = new ArrayList<>();
		Operator.<Comment>perform((dao) -> {
			QueryBuilder<Comment, Integer> qb = dao.queryBuilder()
				.offset(pg.calcCursorIndex())
				.limit(pg.getPageSize());
			Where where = qb.where();
			where.and(
				where.eq(Comment.COL_BLOG, blog),
				APICrudChain.filterOnMode(where, creds));
			retval.addAll((pattern == null)
				? qb.query()
				: dao.queryForMatchingArgs(pattern));
		}, Comment.class);
		return(retval);
	}

	/**
	 * Retrieve a list of tags for the given entity.  If the entity
	 * has no tags, include the root tag.
	 */
	public List<Tag> getTags(final Entity entity, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		List<Tag> retval = chain.getTags(entity, pg);

		if (retval.isEmpty()) {
			retval.add(new Tag());
		}
		return(retval);
	}

	/**
	 * Get a list of tags with a given parent.
	 */
	public List<Tag> getTags(final Tag parent) throws DatastoreException
	{
		final Wrapper<List<Tag>> retval = new Wrapper<>();
		Operator.<Tag>perform((dao) -> {
			int id = parent.getId();
			retval.item = dao.queryForEq(Tag.COL_PARENT,id);
		}, Tag.class);
		return(retval.item);
	}

	public List<Location> getLocations(final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		return(chain.read(Location.class, pg, creds));
	}

	/**
	 * Retrieve a list of locations for the given tag.  If the tag is
	 * the root tag, include locations without explicit tags.
	 */
	public List<Location> getLocations(final Tag tag, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		final List<Location> retval =
			chain.read(Location.class, tag, pg, creds);
		retval.addAll(new OrphanUtil<Location>() {
			public List<Location> execGet(Pagination p)
					throws DatastoreException
			{
				return(getOrphanedLocations(p, creds));
			}
		}.getOrphans(tag, pg));
		return(retval);
	}

	/**
	 * Retrieve a list of events for the given tag.  If the tag is
	 * the root tag, include events without explicit tags.
	 */
	public List<Event> getEvents(final Tag tag, final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		final List<Event> retval = chain.read(Event.class,tag,pg,creds);
		retval.addAll(new OrphanUtil<Event>() {
			public List<Event> execGet(Pagination p)
					throws DatastoreException
			{
				return(getOrphanedEvents(p, creds));
			}
		}.getOrphans(tag, pg));
		return(retval);
	}

	public Session getSession(final String name) throws DatastoreException
	{
		final Wrapper<Session> retval = new Wrapper<>();
		Operator.<Session>perform((dao) -> {
			List<Session> rslt = null;
			rslt = dao.queryForEq(Session.COL_NAME, name);

			if (!rslt.isEmpty()) {
				retval.item = rslt.get(0);
			}
		}, Session.class);
		return(retval.item);
	}

	public void update(Entity entity) throws DatastoreException
	{
		chain.update(entity);
	}

	public void deleteBookmark(final int id) throws DatastoreException
	{
		chain.delete(Bookmark.class, id);
	}

	public void deleteTag(final int id) throws DatastoreException
	{
		chain.delete(Tag.class, id);
	}

	public void deleteLocation(final int id) throws DatastoreException
	{
		chain.delete(Location.class, id);
	}

	public void deleteContact(final int id) throws DatastoreException
	{
		chain.delete(Contact.class, id);
	}

	public void deleteEvent(final int id) throws DatastoreException
	{
		chain.delete(Event.class, id);
	}

	public void deletePhoto(final int id) throws DatastoreException
	{
		chain.delete(Photo.class, id);
	}

	public void deleteFileItem(final int id) throws DatastoreException
	{
		chain.delete(FileItem.class, id);
	}

	public void deleteSound(final int id) throws DatastoreException
	{
		chain.delete(Sound.class, id);
	}

	public void deleteVideo(final int id) throws DatastoreException
	{
		chain.delete(Video.class, id);
	}

	public void deleteBlog(final int id) throws DatastoreException
	{
		chain.delete(Blog.class, id);
	}

	public void deleteComment(final int id) throws DatastoreException
	{
		chain.delete(Comment.class, id);
	}

	public void delete(final Edge edge) throws DatastoreException
	{
		Operator.<Edge>perform((dao) -> {
			DeleteBuilder<Edge, Integer> deleteBuilder =
				dao.deleteBuilder();
			Where where = deleteBuilder.where();
			where.and(
				where.eq(Edge.COL_GROUP,
					edge.getGroup()),
				where.eq(Edge.COL_PARENT,
					edge.getParent()));
			deleteBuilder.delete();
		}, Edge.class);
	}

	public void delete(Entity entity) throws DatastoreException
	{
		chain.delete(entity);
	}

	/**
	 * Remove the entity tags for the given entity.  For example, if the
	 * entity is a bookmark, remove all rows from bookmark_tags where the
	 * bookmark_tags.bookmark equals the bookmark id.
	 */
	public void deleteEntityTags(Entity entity, Credentials creds)
			throws DatastoreException
	{
		Pagination pg = new Pagination();
		boolean flag = true;

		while (flag) {
			for (Tag tag : getTags(pg, creds)) { // KDH
				if (tag.getId() != Tag.ROOT) {
					chain.deleteFromTags(entity, tag);
					// flag = true;
				}
			}
			pg.next();
			flag = false;
		}
	}

	public void deleteEntityTags(Entity entity, long tagId)
			throws DatastoreException
	{
		chain.deleteFromTags(entity, getTag(tagId));
	}

	public boolean hasBookmarkInTag(Bookmark bookmark, Tag tag,
			Credentials creds) throws DatastoreException
	{
		boolean retval = false;
		List<Bookmark> list = null;
		Pagination pg = new Pagination();

		LOOP: while (!(list = getBookmarks(tag, pg, creds)).isEmpty()) {
			for (Bookmark b : list) {
				if (b == bookmark) {
					retval = true;
					break LOOP;
				}
			}
			pg.next();
		}
		return(retval);
	}

	/**
	 * It's possible that by the time this method is called, the tagId
	 * does not resolve anymore.  For this race condition, just ignore the
	 * request.
	 */
	public void add2Tag(Entity entity, long tagId) throws DatastoreException
	{
		Tag tag = null;

		if (entity != null && (tag = getTag(tagId)) != null) {
			chain.add2Tag(entity, tag);
		}
	}

	public List<Blog> getOrphanedBlogs(final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		List<Blog> retval = null;
		QueryBuilder<Blog, Integer> qb1 = null;
		QueryBuilder<BlogTags, Integer> qb2 = null;
		ConnectionSource cs = null;
		Where where = null;
		try {
			cs = new DataSourceConnectionSource(
				Datastore.getDataSource(),
				new MysqlDatabaseType());
			Dao<BlogTags, Integer> blogTagsDAO =
				DaoManager.createDao(cs, BlogTags.class);
			Dao<Blog, Integer> blogDAO = DaoManager.createDao(
				cs, Blog.class);
			qb1 = blogDAO.queryBuilder();
			qb2 = blogTagsDAO.queryBuilder();
			qb1.reset();
			qb2.reset();
			qb2.selectColumns(BlogTags.COL_BLOG);
			qb1.offset(pg.calcCursorIndex());
			qb1.limit(pg.getPageSize());
			where = qb1.where();
			where.and(
				where.notIn(Blog.COL_ID, qb2),
				APICrudChain.filterOnMode(where, creds));
			retval = blogDAO.query(qb1.prepare());
		}
		catch (SQLException e) {
			throw new DatastoreException(e);
		}
		finally {
			if (cs != null) {
				try {
					cs.close();
				}
				catch (Exception se) {
					throw new DatastoreException(se);
				}
			}
		}
		return(retval);
	}

	public List<Bookmark> getOrphanedBookmarks(final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		List<Bookmark> retval = null;
		QueryBuilder<Bookmark, Integer> qb1 = null;
		QueryBuilder<BookmarkTags, Integer> qb2 = null;
		ConnectionSource cs = null;
		Where where = null;
		try {
			cs = new DataSourceConnectionSource(
				Datastore.getDataSource(),
				new MysqlDatabaseType());
			Dao<BookmarkTags, Integer> bookmarkTagsDAO =
				DaoManager.createDao(cs, BookmarkTags.class);
			Dao<Bookmark, Integer> bookmarkDAO =
				DaoManager.createDao(cs, Bookmark.class);
			qb1 = bookmarkDAO.queryBuilder();
			qb2 = bookmarkTagsDAO.queryBuilder();
			qb1.reset();
			qb2.reset();
			qb2.selectColumns(BookmarkTags.COL_BOOKMARK);
			qb1.offset(pg.calcCursorIndex());
			qb1.limit(pg.getPageSize());
			where = qb1.where();
			where.and(
				where.notIn(Bookmark.COL_ID, qb2),
				APICrudChain.filterOnMode(where, creds));
			retval = bookmarkDAO.query(qb1.prepare());
		}
		catch (SQLException e) {
			throw new DatastoreException(e);
		}
		finally {
			if (cs != null) {
				try {
					cs.close();
				}
				catch (Exception se) {
					throw new DatastoreException(se);
				}
			}
		}
		return(retval);
	}

	public List<Location> getOrphanedLocations(final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		List<Location> retval = null;
		QueryBuilder<Location, Integer> qb1 = null;
		QueryBuilder<LocationTags, Integer> qb2 = null;
		ConnectionSource cs = null;
		Where where = null;
		try {
			cs = new DataSourceConnectionSource(
				Datastore.getDataSource(),
				new MysqlDatabaseType());
			Dao<LocationTags, Integer> locTagsDAO =
				DaoManager.createDao(cs, LocationTags.class);
			Dao<Location, Integer> locationDAO =
				DaoManager.createDao(cs, Location.class);
			qb1 = locationDAO.queryBuilder();
			qb2 = locTagsDAO.queryBuilder();
			qb1.reset();
			qb2.reset();
			qb2.selectColumns(LocationTags.COL_LOCATION);
			qb1.offset(pg.calcCursorIndex());
			qb1.limit(pg.getPageSize());
			where = qb1.where();
			where.and(
				where.notIn(Location.COL_ID, qb2),
				APICrudChain.filterOnMode(where, creds));
			retval = locationDAO.query(qb1.prepare());
		}
		catch (SQLException e) {
			throw new DatastoreException(e);
		}
		finally {
			if (cs != null) {
				try {
					cs.close();
				}
				catch (Exception se) {
					throw new DatastoreException(se);
				}
			}
		}
		return(retval);
	}

	public List<Photo> getOrphanedPhotos(final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		List<Photo> retval = null;
		QueryBuilder<Photo, Integer> qb1 = null;
		QueryBuilder<PhotoTags, Integer> qb2 = null;
		ConnectionSource cs = null;
		Where where = null;
		try {
			cs = new DataSourceConnectionSource(
				Datastore.getDataSource(),
				new MysqlDatabaseType());
			Dao<PhotoTags, Integer> photoTagsDAO =
				DaoManager.createDao(cs, PhotoTags.class);
			Dao<Photo, Integer> photoDAO = DaoManager.createDao(
				cs, Photo.class);
			qb1 = photoDAO.queryBuilder();
			qb2 = photoTagsDAO.queryBuilder();
			qb1.reset();
			qb2.reset();
			qb2.selectColumns(PhotoTags.COL_PHOTO);
			qb1.offset(pg.calcCursorIndex());
			qb1.limit(pg.getPageSize());
			where = qb1.where();
			where.and(
				where.notIn(Photo.COL_ID, qb2),
				APICrudChain.filterOnMode(where, creds));
			retval = photoDAO.query(qb1.prepare());
		}
		catch (SQLException e) {
			throw new DatastoreException(e);
		}
		finally {
			if (cs != null) {
				try {
					cs.close();
				}
				catch (Exception se) {
					throw new DatastoreException(se);
				}
			}
		}
		return(retval);
	}

	public List<FileItem> getOrphanedFileItems(final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		List<FileItem> retval = null;
		QueryBuilder<FileItem, Integer> qb1 = null;
		QueryBuilder<FileItemTags, Integer> qb2 = null;
		ConnectionSource cs = null;
		Where where = null;
		try {
			cs = new DataSourceConnectionSource(
				Datastore.getDataSource(),
				new MysqlDatabaseType());
			Dao<FileItemTags, Integer> photoTagsDAO =
				DaoManager.createDao(cs, FileItemTags.class);
			Dao<FileItem, Integer> photoDAO = DaoManager.createDao(
				cs, FileItem.class);
			qb1 = photoDAO.queryBuilder();
			qb2 = photoTagsDAO.queryBuilder();
			qb1.reset();
			qb2.reset();
			qb2.selectColumns(FileItemTags.COL_FILEITEM);
			qb1.offset(pg.calcCursorIndex());
			qb1.limit(pg.getPageSize());
			where = qb1.where();
			where.and(
				where.notIn(FileItem.COL_ID, qb2),
				APICrudChain.filterOnMode(where, creds));
			retval = photoDAO.query(qb1.prepare());
		}
		catch (SQLException e) {
			throw new DatastoreException(e);
		}
		finally {
			if (cs != null) {
				try {
					cs.close();
				}
				catch (Exception se) {
					throw new DatastoreException(se);
				}
			}
		}
		return(retval);
	}

	public List<Sound> getOrphanedSounds(final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		List<Sound> retval = null;
		QueryBuilder<Sound, Integer> qb1 = null;
		QueryBuilder<SoundTags, Integer> qb2 = null;
		ConnectionSource cs = null;
		Where where = null;
		try {
			cs = new DataSourceConnectionSource(
				Datastore.getDataSource(),
				new MysqlDatabaseType());
			Dao<SoundTags, Integer> soundTagsDAO =
				DaoManager.createDao(cs, SoundTags.class);
			Dao<Sound, Integer> soundDAO = DaoManager.createDao(
				cs, Sound.class);
			qb1 = soundDAO.queryBuilder();
			qb2 = soundTagsDAO.queryBuilder();
			qb1.reset();
			qb2.reset();
			qb2.selectColumns(SoundTags.COL_SOUND);
			qb1.offset(pg.calcCursorIndex());
			qb1.limit(pg.getPageSize());
			where = qb1.where();
			where.and(
				where.notIn(Sound.COL_ID, qb2),
				APICrudChain.filterOnMode(where, creds));
			retval = soundDAO.query(qb1.prepare());
		}
		catch (SQLException e) {
			throw new DatastoreException(e);
		}
		finally {
			if (cs != null) {
				try {
					cs.close();
				}
				catch (Exception se) {
					throw new DatastoreException(se);
				}
			}
		}
		return(retval);
	}

	public List<Video> getOrphanedVideos(final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		List<Video> retval = null;
		QueryBuilder<Video, Integer> qb1 = null;
		QueryBuilder<VideoTags, Integer> qb2 = null;
		ConnectionSource cs = null;
		Where where = null;
		try {
			cs = new DataSourceConnectionSource(
				Datastore.getDataSource(),
				new MysqlDatabaseType());
			Dao<VideoTags, Integer> videoTagsDAO =
				DaoManager.createDao(cs, VideoTags.class);
			Dao<Video, Integer> videoDAO = DaoManager.createDao(
				cs, Video.class);
			qb1 = videoDAO.queryBuilder();
			qb2 = videoTagsDAO.queryBuilder();
			qb1.reset();
			qb2.reset();
			qb2.selectColumns(VideoTags.COL_VIDEO);
			qb1.offset(pg.calcCursorIndex());
			qb1.limit(pg.getPageSize());
			where = qb1.where();
			where.and(
				where.notIn(Video.COL_ID, qb2),
				APICrudChain.filterOnMode(where, creds));
			retval = videoDAO.query(qb1.prepare());
		}
		catch (SQLException e) {
			throw new DatastoreException(e);
		}
		finally {
			if (cs != null) {
				try {
					cs.close();
				}
				catch (Exception se) {
					throw new DatastoreException(se);
				}
			}
		}
		return(retval);
	}

	public List<Contact> getOrphanedContacts(final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		List<Contact> retval = null;
		QueryBuilder<Contact, Integer> qb1 = null;
		QueryBuilder<ContactTags, Integer> qb2 = null;
		ConnectionSource cs = null;
		Where where = null;
		try {
			cs = new DataSourceConnectionSource(
				Datastore.getDataSource(),
				new MysqlDatabaseType());
			Dao<ContactTags, Integer> contactTagsDAO =
				DaoManager.createDao(cs, ContactTags.class);
			Dao<Contact, Integer> contactDAO = DaoManager.createDao(
				cs, Contact.class);
			qb1 = contactDAO.queryBuilder();
			qb2 = contactTagsDAO.queryBuilder();
			qb1.reset();
			qb2.reset();
			qb2.selectColumns(ContactTags.COL_BLOG);
			qb1.offset(pg.calcCursorIndex());
			qb1.limit(pg.getPageSize());
			where = qb1.where();
			where.and(
				where.notIn(Contact.COL_ID, qb2),
				APICrudChain.filterOnMode(where, creds));
			retval = contactDAO.query(qb1.prepare());
		}
		catch (SQLException e) {
			throw new DatastoreException(e);
		}
		finally {
			if (cs != null) {
				try {
					cs.close();
				}
				catch (Exception se) {
					throw new DatastoreException(se);
				}
			}
		}
		return(retval);
	}

	public List<Event> getOrphanedEvents(final Pagination pg,
			final Credentials creds) throws DatastoreException
	{
		List<Event> retval = null;
		QueryBuilder<Event, Integer> qb1 = null;
		QueryBuilder<EventTags, Integer> qb2 = null;
		ConnectionSource cs = null;
		Where where = null;
		try {
			cs = new DataSourceConnectionSource(
				Datastore.getDataSource(),
				new MysqlDatabaseType());
			Dao<EventTags, Integer> eventTagsDAO =
				DaoManager.createDao(cs, EventTags.class);
			Dao<Event, Integer> eventDAO = DaoManager.createDao(
				cs, Event.class);
			qb1 = eventDAO.queryBuilder();
			qb2 = eventTagsDAO.queryBuilder();
			qb1.reset();
			qb2.reset();
			qb2.selectColumns(EventTags.COL_EVENT);
			qb1.offset(pg.calcCursorIndex());
			qb1.limit(pg.getPageSize());
			where = qb1.where();
			where.and(
				where.notIn(Event.COL_ID, qb2),
				APICrudChain.filterOnMode(where, creds));
			retval = eventDAO.query(qb1.prepare());
		}
		catch (SQLException e) {
			throw new DatastoreException(e);
		}
		finally {
			if (cs != null) {
				try {
					cs.close();
				}
				catch (Exception se) {
					throw new DatastoreException(se);
				}
			}
		}
		return(retval);
	}

	/**
	 * Search the view for entities.
	 *
	 * @param pg - A Pagination instance.
	 * @param creds - The credential used for security.
	 * @param tag - The tag that should be used to limit the search or null.
	 * @param clazz - The class that should be used to limit search or null.
	 * @param pattern - The pattern to search for.
	 * @return List<APIEntity> - The list of entities found.
	 */
	public List<APIEntity> search(final Pagination pg,
			final Credentials creds, final Tag tag, String clazz,
			String pattern) throws DatastoreException
	{
		final List<APIEntity> retval = new ArrayList<>();
		final List<String> args = new ArrayList<>();
		String maskColumn = APIEntity.COL_MASK;
		Group group = creds.getGroup();
		group = (group != null) ? group : new Group();
		boolean flag = false;
		final StringBuilder sql = new StringBuilder().append(""
			+          "SELECT class, id "
			+            "FROM search_view "
			+((flag = (clazz != null))
			?           "WHERE class = ? "
			:               ""));

		if (flag) {
			args.add(clazz);
		}
		sql.append((tag == null)
			?               ""
			:   ((flag)
			?             "AND "
			:           "WHERE ")+"tag = ? ");
		flag |= (tag != null);

		if (tag != null) {
			args.add(""+tag.getId());
		}
		sql.append((pattern == null)
			?               ""
			:   ((flag)
			?             "AND "
			:           "WHERE ")+"haystack LIKE ? ");
		flag |= (pattern != null);

		if (pattern != null) {
			args.add("%"+pattern+"%");
		}
		sql.append(((flag)
			?             "AND "
			:           "WHERE ")+"(");
		sql.append(               "(user = "+creds.getId()+" "
			+                 "AND "+maskColumn+" >= 400) ");
		sql.append(            "OR (group_ = "+group.getId()+" "
			+                 "AND ("+maskColumn+" % 100) >= 40) ");
		sql.append(            "OR ("+maskColumn+" % 10) >= 4) ");
		sql.append(         "LIMIT "+pg.getPageSize()+" ");
		sql.append(        "OFFSET "+pg.calcCursorIndex()+" ");
		logger.debug("SQL: {}", ""+sql);

		Operator.<Blog>perform((dao) -> {
			String[] a = new String[args.size()];
			retval.addAll(dao.queryRaw(""+sql, rawRowMapper,
				args.toArray(a)).getResults());
		}, Blog.class);
		return(retval);
	}

	/**
	 * Decrease the total amount of storage available to this user.
	 *
	 * @param creds - The user.
	 * @param amount - The amount to decrease by.
	 */
	public void consumeQuota(final Credentials creds, final long amount)
			throws DatastoreException
	{
		Operator.<Blog>perform((dao) -> {
			dao.updateRaw(""
				+"UPDATE quota "
				+   "SET available = (available - ?), "
				+       "used = (used + ?) "
				+ "WHERE user = ? ", ""+amount,
				""+amount, ""+creds.getId());
		}, Blog.class);
	}

	/**
	 * Set the total amount of storage available to this user.
	 *
	 * @param creds - The user.
	 * @param amount - The amount to set.
	 */
	public void setAvailableQuota(final Credentials creds,
			final long available) throws DatastoreException
	{
		Operator.<Blog>perform((dao) -> {
			dao.updateRaw(""
				+"UPDATE quota "
				+   "SET available = ? "
				+ "WHERE user = ? ", ""+available,
				""+creds.getId());
		}, Blog.class);
	}

	/**
	 * Get the total amount of storage available to this user.
	 *
	 * @param creds - The user.
	 * @return long - The amount.
	 */
	public long getAvailableQuota(final Credentials creds)
			throws DatastoreException
	{
		final Wrapper<Long> retval = new Wrapper<>();
		Operator.<Blog>perform((dao) -> {
			retval.item = new Long(dao.queryRawValue(""
				+"SELECT available "
				+  "FROM quota "
				+ "WHERE user = ? ",""+creds.getId()));
		}, Blog.class);
		return(retval.item);
	}

	/**
	 * Get the landing page for this group.
	 *
	 * @param group - The group.
	 * @return String - The landing page (URL).
	 */
	public String getLandingPage(final Group group)
			throws DatastoreException
	{
		final Wrapper<String> retval = new Wrapper<>();
		Operator.<Blog>perform((dao) -> {
			String[] rslt = dao.queryRaw(""
				+"SELECT url "
				+  "FROM landing "
				+ "WHERE group_ = ? ",
				""+group.getId()).getFirstResult();
			retval.item = (rslt != null)
				? (rslt.length > 0) ? rslt[0] : null
				: null;
		}, Blog.class);
		return(retval.item);
	}

	/**
	 * Set the landing page for this group.
	 *
	 * @param group - The group.
	 * @param String - The landing page (URL).
	 */
	public void setLandingPage(final Group group, final String landing)
			throws DatastoreException
	{
		Operator.<Blog>perform((dao) -> {
			dao.updateRaw(""
				+"DELETE FROM landing "
				+      "WHERE group_ = ?",
				""+group.getId());
			dao.updateRaw(""
				+"INSERT INTO landing ("
				+                "url, group_"
				+             ") "
				+     "VALUES ("
				+                "?, ? "
				+             ")", landing,
				""+group.getId());
		}, Blog.class);
	}

	/**
	 * Log the login time for the specified user.
	 *
	 * @param creds - The user.
	 */
	public void noteLogin(Credentials creds) throws DatastoreException
	{
		noteLogin(creds, null);
	}

	/**
	 * Log the login time and browser agent for the specified user.
	 *
	 * @param creds - The user.
	 * @param agent - The browser agent as passed in by the client.
	 */
	public void noteLogin(final Credentials creds, final String agent)
			throws DatastoreException
	{
		Operator.<Blog>perform((dao) -> {
			String fmt = "yyyy-MM-dd HH:mm:ss";
			String now = new SimpleDateFormat(fmt).format(
				new Date());
			dao.updateRaw(""
				+"INSERT INTO last_login ("
				+                "user, began, agent"
				+             ") "
				+     "VALUES ("
				+                "?, ?, ? "
				+             ")", ""+creds.getId(),
				now, agent);
		}, Blog.class);
	}

	protected RawRowMapper<APIEntity> rawRowMapper = (cols, rslts) -> {
		APIEntity retval = null;
		String clazz = null;
		int id = -1;
		logger.trace("RawRowMapper results: {}",Arrays.toString(rslts));
		try {
			id = Integer.parseInt(rslts[1]);

			switch ((clazz = rslts[0])) {
			case "blog":
				retval = getBlog(id);
				break;
			case "bookmark":
				retval = getBookmark(id);
				break;
			case "contact":
				retval = getContact(id);
				break;
			case "event":
				retval = getEvent(id);
				break;
			case "fileitem":
				retval = getFileItem(id);
				break;
			case "location":
				retval = getLocation(id);
				break;
			case "photo":
				retval = getPhoto(id);
				break;
			case "sound":
				retval = getSound(id);
				break;
			default:
				throw new RuntimeException("unhandled "
					+"search class \""+clazz+"\"");
			}
		}
		catch (Exception e) {
			logger.error(""+e, e);
		}
		return(retval);
	};

	/**
	 * Determine whether the specified user is in the given group.
	 *
	 * @param creds - The user.
	 * @param group - The group.
	 * @return boolean - Is the user in this group?
	 */
	public boolean userInGroup(Credentials creds, Group group)
			throws DatastoreException
	{
		boolean retval = false;

		for (Group found : getGroups(creds)) {
			if (found.equals(group) && !found.isDisabled()) {
				retval = true;
				break;
			}
		}
		return(retval);
	}
}
