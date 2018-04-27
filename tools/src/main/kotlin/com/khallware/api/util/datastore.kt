// Copyright Kevin D.Hall 2018

package com.khallware.api.util

import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties

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
		initialize()
		val statement = connection!!.createStatement()
		val sql = "SELECT id,name FROM tags"
		var rsltSet = statement.executeQuery(sql)

		while(rsltSet.next()) {
			with (rsltSet) {
				retval.add(Tag(getInt("id"), getString("name")))
			}
		}
		statement.close()
		return(retval)
	}

	fun listBookmarks(tag : Tag) : ArrayList<Bookmark>
	{
		val retval = ArrayList<Bookmark>()
		initialize()
		val statement = connection!!.createStatement()
		val sql = """
			   SELECT b.id,name,url
			     FROM bookmarks b
			LEFT JOIN bookmark_tags bt
			       ON b.id = bt.bookmark
			    WHERE bt.tag = { tag.id }
		"""
		var rsltSet = statement.executeQuery(sql)

		while(rsltSet.next()) {
			with (rsltSet) {
				retval.add(Bookmark(
					getInt("id"),
					getString("name"),
					getString("url")))
			}
		}
		statement.close()
		return(retval)
	}
}
