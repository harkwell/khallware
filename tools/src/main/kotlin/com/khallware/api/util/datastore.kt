// Copyright Kevin D.Hall 2018

package com.khallware.api.util

import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties
import java.math.BigDecimal

class Datastore(props: Properties)
{
	var initialized = false
	var connection : Connection? = null
	val jdbcurl = props.getProperty("jdbc_url","jdbc:sqlite:apis.db")
	val driver = props.getProperty("jdbc_driver", "org.sqlite.JDBC")

	fun initialize()
	{
		if (!initialized) {
			Class.forName(driver).newInstance()
			connection = DriverManager.getConnection(jdbcurl)
		}
		initialized = true
	}

	fun listTags() : ArrayList<Tag>
	{
		val retval = ArrayList<Tag>()
		val sql = "SELECT id,name FROM tags"
		initialize()
		connection!!.createStatement().use {
			var rsltSet = it.executeQuery(sql)

			while (rsltSet.next()) {
				with (rsltSet) {
					retval.add(Tag(
						getInt("id"),
						getString("name")))
				}
			}
		}
		return(retval)
	}

	fun listBookmarks() : ArrayList<Bookmark>
	{
		val retval = ArrayList<Bookmark>()
		val sql = """
			   SELECT id,name,url, (
					SELECT count(*)
					  FROM bookmark_tags
					 WHERE bookmark = b.id
				) AS numtags
			     FROM bookmarks b
		"""
		initialize()
		connection!!.createStatement().use {
			var rsltSet = it.executeQuery(sql)

			while (rsltSet.next()) {
				with (rsltSet) {
					retval.add(Bookmark(
						getInt("id"),
						getString("name"),
						getString("url"),
						getInt("numtags")))
				}
			}
		}
		return(retval)
	}

	fun listLocations() : ArrayList<Location>
	{
		val retval = ArrayList<Location>()
		val sql = """
			   SELECT id,name,latitude,longitude,address,
			          description, (
					SELECT count(*)
					  FROM location_tags
					 WHERE location = l.id
				) AS numtags
			     FROM locations l
		"""
		initialize()
		connection!!.createStatement().use {
			var rsltSet = it.executeQuery(sql)

			while (rsltSet.next()) {
				with (rsltSet) {
					retval.add(Location(
						getInt("id"),
						getString("name"),
						BigDecimal(getDouble("lat")),
						BigDecimal(getDouble("lon")),
						getString("address"),
						getString("desc"),
						getInt("numtags")))
				}
			}
		}
		return(retval)
	}
}
