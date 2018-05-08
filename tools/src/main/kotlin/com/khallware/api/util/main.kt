@file:JvmName("Main")

// Copyright Kevin D.Hall 2018

package com.khallware.api.util

import com.xenomachina.argparser.mainBody
import com.xenomachina.argparser.ArgParser
import org.gagravarr.vorbis.VorbisPacketFactory;
import org.gagravarr.vorbis.VorbisComments;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.ogg.OggFile;
import org.slf4j.LoggerFactory
import kotlin.text.Regex
import java.net.URL
import java.net.Socket
import java.net.InetSocketAddress
import java.security.MessageDigest
import java.io.FileInputStream
import java.io.InputStream
import java.io.File
import java.io.FileReader
import java.io.BufferedReader
import java.sql.DriverManager
import java.util.Properties
import java.util.Hashtable
import java.nio.file.Files
import java.math.BigInteger
import javax.naming.NamingEnumeration
import javax.naming.directory.InitialDirContext
import javax.naming.directory.Attributes

/**
 * This utility scans and validates webservice entities.  For each bookmark,
 * it connects to the server to enforce availability and will warn the user
 * for failed connections.  It does not automatically delete them unless the
 * force option is given.  Locations, events blogs and comments have no need
 * for validation.  The domain part of an email address is verified and the
 * user is warned for MX name resolution failures.  The contact is never
 * removed.  For photos, the path and md5sums are checked.  When the file
 * is not present, or present but has a mismatching md5sum, the user is
 * warned.  The mime type consistency between content and filename extension
 * is also enforced. Photos are not deleted from the database unless the force
 * option is given.  File items are treated the same way that photos are
 * except the mime type from the database is used instead of the filename
 * extension.  Sounds and videos are enforced the same way that photos are.
 *
 * The utility also traverses the filesystems specified in the properties
 * file.  It will warn if any file found there is not also in the database.
 * It will add the item if the add option is specified.  For html files, the
 * anchors are parsed and added as bookmarks.  For text files, any line that
 * matches the pattern of an http or https URL is added as a bookmark.  For
 * kml files, the relevant location information is added to the database.
 * For vcf files, contacts are added.  For ics files, events are added. For
 * jpg files, photos are added.  For ogg vorbis files, sounds are added.
 * For mp4 files, videos are added. For any other file, a file item is added.
 * The sub-directory name is used as the tag relation from above unless it
 * is overridden by the tag option.
 *
 * By default the utility will leverage the web services, but with the
 * direct option will read and write directly from/to the database.
 *
 */
class LogAnchor
{
	val logger = LoggerFactory.getLogger(LogAnchor::class.java)
}

class ToolArgs(parser: ArgParser)
{
	val add by parser.flagging(
		"-a", "--add", help = "add any found items")

	val force by parser.flagging(
		"-f", "--force", help = "force operation due to caution")

	val tagname by parser.storing(
		"-t", "--tag-name", help = "import under specified tagname"
	) //.default("website")

	val propfile by parser.storing(
			"-p", "--propfile", help = "khallware properties file")
		/* .default("/tmp/main.properties")
		.addValidator {
			if (!File("${ propfile) }".exists()) {
				throw RuntimeException(
					"File not found: ${ propfile }")
			}
		} */
}

val logger = LogAnchor().logger
val props = Properties()
var tagid = -1

fun getFilespecs() : Array<String>
{
	return(arrayOf(
		props.getProperty("images","/dev/null"),
		props.getProperty("thumbs","/dev/null"),
		props.getProperty("audio","/dev/null"),
		props.getProperty("upload.dir","/dev/null")))
}

fun findUrls(filespecs: Array<String>) : List<String>
{
	val retval = ArrayList<String>()

	for (filespec in filespecs) {
		for (src in File(filespec).walkTopDown()) {
			for (url in parseFiles4Urls(src.toString())) {
				logger.debug("found url: {}", url)
				retval.add(url)
			}
		}
	}
	return(retval)
}

fun findFilesWithSuffix(dir: String, vararg exts: String) : List<String>
{
	val retval = ArrayList<String>()

	for (src in File(dir).walkTopDown()) {
		var found = false

		for (ext in exts) {
			if ((""+src).toLowerCase().endsWith(ext)) {
				found = true
				break
			}
		}
		if (found) {
			logger.debug("found file: {}", src)
			retval.add(""+src)
		}
	}
	return(retval)
}

fun findPhotos(dir: String) : List<String>
{
	return(findFilesWithSuffix(dir,"jpg","jpeg"))
}

fun findSounds(dir: String) : List<String>
{
	return(findFilesWithSuffix(dir,"ogg"))
}

fun findVideos(dir: String) : List<String>
{
	return(findFilesWithSuffix(dir,"mg4","mpg","mpeg"))
}

fun findFileItems(dir: String) : List<String>
{
	val retval = ArrayList<String>()
	val antiexts = arrayOf("jpg", "jpeg", "ogg", "ics", "vcf", "html",
		"htm", "kml", "kmz", "mp4", "mpeg")

	for (src in File(dir).walkTopDown()) {
		for (ext in antiexts) {
			if ((""+src).toLowerCase().endsWith(ext)) {
				continue
			}
		}
		logger.debug("found file: {}", src)
		retval.add(""+src)
	}
	return(retval)
}

fun parseFiles4Urls(fname: String) : List<String>
{
	val retval = ArrayList<String>()
	val regex = Regex(".*(?<url>http[s]*://[A-z0-9.]+[/]*).*")
	var line = ""

	if (File(fname).isFile()) {
		logger.info("parsing URLs from file: {}", fname)
		BufferedReader(FileReader(fname)).use {
			while ({ line = it.readLine() ?: "_" ;line }() != "_") {
				if (regex.matches(line)) {
					val found = regex.matchEntire(
						line)!!.groups.get(
						"url")!!.value

					if (validUrl(found)) {
						retval.add(found)
					}
				}
			}
		}
	}
	return(retval)
}

fun validUrl(u: String) : Boolean
{
	var retval = false
	logger.debug("testing url: '{}'", u)
	try {
		val url = URL(u)
		var port = url.getPort()

		if (port == -1) {
			port = 80;
		}
		Socket().use {
			it.connect(InetSocketAddress(url.getHost(), port))
			retval = it.isConnected()
		}
	}
	catch (e: Exception) {
		logger.trace("${ e }", e)
		logger.warn("${ e } ${ u }")
	}
	return(retval)
}

fun listMXRecords(domain: String) : List<String>
{
	val retval = ArrayList<String>()
	val ht = Hashtable<String,String>()
	ht.put("java.naming.factory.initial",
		"com.sun.jndi.dns.DnsContextFactory")
	try {
		val attrs = InitialDirContext(ht).getAttributes(
			domain, arrayOf("MX"))

		if (attrs.get("MX") != null) {
			val ne = attrs.get("MX").getAll()

			while (ne.hasMore()) {
				val dat = "${ ne.next() }".split(" ")

				if (dat[1].endsWith(".")) {
					retval.add(dat[1].substring(
						0,(dat[1].length - 1)))
				}
				else {
					retval.add(dat[1])
				}
			}
		}
	}
	catch (e: Exception) {
		logger.debug("{}", e)
	}
	return(retval)
}

fun validEmail(email: String) : Boolean
{
	var retval = false
	logger.debug("testing email: '{}'", email)
	try {
		val domain = email.split("@")[1]

		for (host in listMXRecords(domain)) {
			Socket().use {
				it.connect(InetSocketAddress(host, 25))

				if (it.isConnected()) {
					retval = true
				}
			}
			if (retval) {
				break
			}
		}
	}
	catch (e: Exception) {
		logger.trace("${ e }", e)
		logger.warn("${ e } ${ email }")
	}
	return(retval)
}

fun md5sumGenerate(filespec: String) : String
{
	var retval = ""
	// logger.trace("generating md5sum for file: '{}'", filespec)
	try {
		val md = MessageDigest.getInstance("MD5")
		val hash = md.digest(Files.readAllBytes(
			File(filespec).toPath()));
		val rslt = String.format("%032x", BigInteger(1,hash));
		// logger.trace("md5sum is {}", rslt)
	}
	catch (e: Exception) {
		logger.trace("${ e }", e)
		logger.warn("${ e } ${ filespec }")
	}
	return(retval)
}

fun md5sumMatches(filespec: String, md5sum: String) : Boolean
{
	var retval = false
	var generated = md5sumGenerate(filespec)
	logger.debug("md5sum comparison (found={} given={})", generated, md5sum)
	retval = (!generated.isEmpty() && !md5sum.isEmpty()
		&& generated.equals(md5sum))
	return(retval)
}

fun sanitycheck()
{
	var healthy = true
	val jdbcURL = props.getProperty("jdbc_url")

	for (dir in getFilespecs()) {
		if (!Files.exists(File(dir).toPath())) {
			logger.error("directory ({}) does not exist!", dir)
			healthy = false
		}
	}
	Datastore(props).initialize()

	if (!DriverManager.getConnection(jdbcURL).isValid(5000)) {
		logger.error("failed to connect to: {}", jdbcURL)
		healthy = false
	}
	if (!healthy) {
		throw IllegalStateException("sanitycheck failed")
	}
}

fun validateExistingBookmarks()
{
	for (bookmark in Datastore(props).listBookmarks()) {
		logger.debug("bookmark: {}", bookmark)

		if (!validUrl(bookmark.url)) {
			logger.warn("Failed to connect: {}", bookmark)
		}
		else if (bookmark.numtags < 1) {
			logger.warn("Orphaned item: {}", bookmark)
		}
	}
}

fun validateExistingContactEmail()
{
	for (contact in Datastore(props).listContacts()) {
		logger.debug("contact: {}", contact)

		if (!validEmail(contact.email)) {
			logger.warn("Failed to resolve: {}", contact.email)
		}
		else if (contact.numtags < 1) {
			logger.warn("Orphaned item: {}", contact)
		}
	}
}

fun validateExistingPhotos()
{
	for (photo in Datastore(props).listPhotos()) {
		logger.debug("photo: {}", photo)

		if (!md5sumMatches(photo.path, photo.md5sum)) {
			logger.warn("File content mismatches: {}", photo.path)
		}
		else if (photo.numtags < 1) {
			logger.warn("Orphaned item: {}", photo)
		}
	}
}

fun validateExistingFileItems()
{
	for (fileitem in Datastore(props).listFileItems()) {
		logger.debug("fileitem: {}", fileitem)

		if (!md5sumMatches(fileitem.path, fileitem.md5sum)) {
			logger.warn("File content mismatches: {}",fileitem.path)
		}
		else if (fileitem.numtags < 1) {
			logger.warn("Orphaned item: {}", fileitem)
		}
	}
}

fun validateExistingSounds()
{
	for (sound in Datastore(props).listSounds()) {
		logger.debug("sound: {}", sound)

		if (!md5sumMatches(sound.path, sound.md5sum)) {
			logger.warn("File content mismatches: {}", sound.path)
		}
		else if (sound.numtags < 1) {
			logger.warn("Orphaned item: {}", sound)
		}
	}
}

fun validateExistingVideos()
{
	for (video in Datastore(props).listVideos()) {
		logger.debug("video: {}", video)

		if (!md5sumMatches(video.path, video.md5sum)) {
			logger.warn("File content mismatches: {}", video.path)
		}
		else if (video.numtags < 1) {
			logger.warn("Orphaned item: {}", video)
		}
	}
}

fun validateExisting()
{
	validateExistingBookmarks()
	validateExistingContactEmail()
	validateExistingPhotos()
	validateExistingFileItems()
	validateExistingSounds()
	validateExistingVideos()
}

fun searchAndAddBookmarks()
{
	val bookmarks = Datastore(props).listBookmarks()

	for (url in findUrls(getFilespecs())) {
		var found = false

		for (bookmark in bookmarks) {
			if (bookmark.url.equals(url)) {
				logger.warn("duplicate bookmark: {}", bookmark)
				found = true
				break
			}
		}
		if (!found) {
			val b = Bookmark(-1, url, url)
			Datastore(props).addBookmark(b, Math.max(1, tagid))
			bookmarks.add(b)
		}
	}
}

fun searchAndAddFileItems()
{
	val fileitems = Datastore(props).listFileItems()

	for (filespec in findFileItems(
			props.getProperty("upload.dir","/dev/null"))) {
		var found = false

		for (fileitem in fileitems) {
			if (fileitem.path.equals(fileitem)) {
				logger.warn("duplicate fileitem: {}", fileitem)
				found = true
				break
			}
		}
		if (!found && File(filespec).isFile()) {
			val f = FileItem(-1, File(filespec).getName(), filespec,
				md5sumGenerate(filespec), filespec, "*/*",
				filespec, -1)
			Datastore(props).addFileItem(f, Math.max(1, tagid))
			fileitems.add(f)
		}
	}
}

fun searchAndAddPhotos()
{
	val photos = Datastore(props).listPhotos()

	for (jpeg in findPhotos(props.getProperty("images","/dev/null"))) {
		var found = false

		for (photo in photos) {
			if (md5sumMatches(jpeg, photo.md5sum)) {
				logger.warn("duplicate photo: {}", photo)
				found = true
				break
			}
		}
		if (!found) {
			val md5sum = md5sumGenerate(jpeg)
			val p = Photo(-1, jpeg, jpeg, md5sum, jpeg)
			logger.debug("photo: {}", p)
			Datastore(props).addPhoto(p, Math.max(1,tagid))
			photos.add(p)
		}
	}
}

fun getVorbisComments(oggfile: String) : VorbisComments
{
	var retval: VorbisComments = VorbisComments()
	OggFile(FileInputStream(oggfile)).use {
		val reader =OggPacketReader(it.getPacketReader() as InputStream)
		reader.getNextPacket()
		val vpacket = VorbisPacketFactory.create(reader.getNextPacket())
		retval = vpacket as VorbisComments
	}
	return(retval)
}

fun searchAndAddSounds()
{
	val sounds = Datastore(props).listSounds()

	for (oggfile in findSounds(props.getProperty("images","/dev/null"))) {
		var found = false

		for (sound in sounds) {
			if (md5sumMatches(oggfile, sound.md5sum)) {
				logger.warn("duplicate sound: {}", sound)
				found = true
				break
			}
		}
		if (!found) {
			val md5sum = md5sumGenerate(oggfile)
			val vc = getVorbisComments(oggfile)
			val s = Sound(-1, oggfile, oggfile, md5sum, oggfile,
				vc.getTitle(), vc.getArtist(), vc.getGenre(),
				vc.getAlbum(), "", -1)
			logger.debug("sound: {}", s)
			Datastore(props).addSound(s, Math.max(1,tagid))
			sounds.add(s)
		}
	}
}

fun searchAndAddVideos()
{
	val videos = Datastore(props).listVideos()

	for (vidfile in findVideos(props.getProperty("images","/dev/null"))) {
		var found = false

		for (video in videos) {
			if (md5sumMatches(vidfile, video.md5sum)) {
				logger.warn("duplicate video: {}", video)
				found = true
				break
			}
		}
		if (!found) {
			val md5sum = md5sumGenerate(vidfile)
			val vc = getVorbisComments(vidfile)
			val v = Video(-1, File(vidfile).getName(), vidfile,
				md5sum, vidfile, -1)
			logger.debug("video: {}", v)
			Datastore(props).addVideo(v, Math.max(1,tagid))
			videos.add(v)
		}
	}
}

fun searchAndAdd()
{
	searchAndAddBookmarks()
	searchAndAddPhotos()
	searchAndAddFileItems()
	searchAndAddSounds()
	searchAndAddVideos()
}

fun main(args: Array<String>) = mainBody {
	ArgParser(args).parseInto(::ToolArgs).run {
		props.load(FileInputStream(propfile))
		logger.debug("props: {}", props)
		sanitycheck()

		for (tag in Datastore(props).listTags()) {
			logger.debug("tag: {}", tag)

			if (tagname.equals(tag.name)) {
				tagid = tag.id
			}
		}
		validateExisting()

		if (add) {
			logger.info("adding files found in {}", getFilespecs())
			searchAndAdd()
		}
	}
}
