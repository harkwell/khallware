// Copyright Kevin D.Hall 2018

package com.khallware.api.util

import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.Table
import java.math.BigDecimal
import java.sql.Connection
import java.nio.file.Files
import java.io.File

object Tags : Table("tags")
{
	val id = integer("id").autoIncrement().primaryKey()
	val name = varchar("name", 1024)
}

object Bookmarks : Table("bookmarks")
{
	val id = integer("id").autoIncrement().primaryKey()
	val name = varchar("name", 50)
	val url = varchar("url", 1024)
}

object BookmarkTags : Table("bookmark_tags")
{
	val id = integer("id").autoIncrement().primaryKey()
	val bookmark = integer("bookmark") references Bookmarks.id
	val tag = integer("tag") references Tags.id
}

class Datastore(val jdbcurl: String = "jdbc:sqlite:apis.db",
		val driver: String = "org.sqlite.JDBC")
{
	var initialized = false

	fun initialize()
	{
		if (!initialized) {
			Database.connect(jdbcurl, driver)

			if (driver.contains("sqlite")) {
			   TransactionManager.manager.defaultIsolationLevel =
				Connection.TRANSACTION_SERIALIZABLE
			}
		}
		initialized = true
	}

	fun listTags() : ArrayList<Tag>
	{
		val retval = ArrayList<Tag>()
		initialize()

		transaction {
			for (rslt in Tags.selectAll()) {
				retval.add(Tag(rslt[Tags.id], rslt[Tags.name]))
			}
		}
		return(retval)
	}

	fun listBookmarks(tag : Tag) : ArrayList<Bookmark>
	{
		val retval = ArrayList<Bookmark>()
		initialize()

		transaction {
			for (rslt in BookmarkTags.innerJoin(Bookmarks).select {
					BookmarkTags.tag eq tag.id }){
				retval.add(
					Bookmark(rslt[Bookmarks.id],
						rslt[Bookmarks.name],
						rslt[Bookmarks.url]))
			}
		}
		return(retval)
	}
}
