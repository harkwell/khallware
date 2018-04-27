@file:JvmName("Main")

// Copyright Kevin D.Hall 2018

package com.khallware.api.util

import org.slf4j.LoggerFactory
// import kotlin.text.Regex
import java.net.URL
import java.io.File
import java.io.FileInputStream
import java.sql.DriverManager
import java.util.Properties
import java.nio.file.Files

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

val logger = LogAnchor().logger
val props = Properties()

var doForce = false
var doAdd = false
var useWeb = true
var tagname = "unknown"
var url = ""

fun parseArgs(args: Array<String>)
{
	for (arg in args) {
		when (arg.substring(0,1)) {
			"-w" -> useWeb = !useWeb
			"-a" -> doAdd = !doAdd
			"-f" -> doForce = !doForce
			"-u" -> url = arg.substring(2)
			"-t" -> tagname = arg.substring(2)
			else -> logger.error("unknown arg: {}", arg)
		}
	}
}

fun sanitycheck()
{
	var healthy = true
	val imagesDir = props.getProperty("images")
	val thumbsDir = props.getProperty("thumbs")
	val audioDir = props.getProperty("audio")
	val uploadDir = props.getProperty("upload.dir")

	for (dir in arrayOf(imagesDir, thumbsDir, audioDir, uploadDir)) {
		if (!Files.exists(File(dir).toPath())) {
			logger.error("directory ({}) does not exist!", dir)
			healthy = false
		}
	}
	if (useWeb) {
		/*
		if (!URL(url).openStream().) {
			logger.error("failed to connect to: {}", url)
			healthy = false
		}
		*/
	}
	else {
		val jdbcURL = props.getProperty("jdbc_url")
		Datastore().initialize()

		if (!DriverManager.getConnection(jdbcURL).isValid(5000)) {
			logger.error("failed to connect to: {}", jdbcURL)
			healthy = false
		}
	}
	if (!healthy) {
		throw IllegalStateException("sanitycheck failed")
	}
}

fun main(args: Array<String>)
{
	parseArgs(args.sliceArray(0 until (args.size - 1)))
	props.load(FileInputStream(args[args.size - 1]))
	logger.debug("props: {}", props)
	sanitycheck()

	for (tag in Datastore().listTags()) {
		logger.debug("tag: {}", tag)

		for (bookmark in Datastore().listBookmarks(tag)) {
			logger.debug("bookmark: {}", bookmark)
		}
	}
}
