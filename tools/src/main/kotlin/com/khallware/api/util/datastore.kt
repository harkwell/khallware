// Copyright Kevin D.Hall 2018

package com.khallware.api.util

import java.sql.DriverManager
import java.sql.Connection
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
			   SELECT id,name,url,rating, (
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
						getString("rating"),
						getInt("numtags")))
				}
			}
		}
		return(retval)
	}

	fun addBookmark(bookmark: Bookmark, tag: Int = 1)
	{
		val sql1 = """
			INSERT INTO bookmarks (name,user,url,group_,rating)
			     VALUES (?,?,?,?,?)
		"""
		val sql2 = """
			INSERT INTO bookmark_tags (bookmark,tag)
			     VALUES (?,?)
		"""
		var id = -1
		initialize()
		connection!!.prepareStatement(sql1).use {
			it.setString(1, bookmark.name)
			it.setInt(2, bookmark.user)
			it.setString(3, bookmark.url)
			it.setInt(4, bookmark.group)
			it.setString(5, bookmark.rating)
			it.execute()
			id = it.getGeneratedKeys().getInt(1)
		}
		connection!!.prepareStatement(sql2).use {
			it.setInt(1, id)
			it.setInt(2, tag)
			it.execute()
		}
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
						getString("description"),
						getInt("numtags")))
				}
			}
		}
		return(retval)
	}

	fun listContacts() : ArrayList<Contact>
	{
		val retval = ArrayList<Contact>()
		val sql = """
			   SELECT id,name,uid,email,phone,title,address,
			          organization,vcard,description, (
					SELECT count(*)
					  FROM contact_tags
					 WHERE contact = c.id
				) AS numtags
			     FROM contacts c
		"""
		initialize()
		connection!!.createStatement().use {
			var rsltSet = it.executeQuery(sql)

			while (rsltSet.next()) {
				with (rsltSet) {
					retval.add(Contact(
						getInt("id"),
						getString("name"),
						getString("uid"),
						getString("email"),
						getString("phone"),
						getString("title"),
						getString("address"),
						getString("organization"),
						getString("vcard"),
						getString("description"),
						getInt("numtags")))
				}
			}
		}
		return(retval)
	}

	fun listPhotos() : ArrayList<Photo>
	{
		val retval = ArrayList<Photo>()
		val sql = """
			   SELECT id,name,path,md5sum,description, (
					SELECT count(*)
					  FROM photo_tags
					 WHERE photo = p.id
				) AS numtags
			     FROM photos p
		"""
		initialize()
		connection!!.createStatement().use {
			var rsltSet = it.executeQuery(sql)

			while (rsltSet.next()) {
				with (rsltSet) {
					retval.add(Photo(
						getInt("id"),
						getString("name"),
						getString("path"),
						getString("md5sum"),
						getString("description"),
						getInt("numtags")))
				}
			}
		}
		return(retval)
	}

	fun addPhoto(photo: Photo, tag: Int = 1)
	{
		val sql1 = """
			INSERT INTO photos (name,path,md5sum,user,group_,
			            description)
			     VALUES (?,?,?,?,?,?)
		"""
		val sql2 = """
			INSERT INTO photo_tags (photo,tag)
			     VALUES (?,?)
		"""
		var id = -1
		initialize()
		connection!!.prepareStatement(sql1).use {
			it.setString(1, photo.name)
			it.setString(2, photo.path)
			it.setString(3, photo.md5sum)
			it.setInt(4, photo.user)
			it.setInt(5, photo.group)
			it.setString(6, photo.desc)
			it.execute()
			id = it.getGeneratedKeys().getInt(1)
		}
		connection!!.prepareStatement(sql2).use {
			it.setInt(1, id)
			it.setInt(2, tag)
			it.execute()
		}
	}

	fun listFileItems() : ArrayList<FileItem>
	{
		val retval = ArrayList<FileItem>()
		val sql = """
			   SELECT id,name,ext,mime,path,md5sum,description, (
					SELECT count(*)
					  FROM fileitem_tags
					 WHERE fileitem = f.id
				) AS numtags
			     FROM fileitems f
		"""
		initialize()
		connection!!.createStatement().use {
			var rsltSet = it.executeQuery(sql)

			while (rsltSet.next()) {
				with (rsltSet) {
					retval.add(FileItem(
						getInt("id"),
						getString("name"),
						getString("ext"),
						getString("md5sum"),
						getString("description"),
						getString("mime"),
						getString("path"),
						getInt("numtags")))
				}
			}
		}
		return(retval)
	}

	fun listSounds() : ArrayList<Sound>
	{
		val retval = ArrayList<Sound>()
		val sql = """
			   SELECT id,name,path,md5sum,title,artist,genre,
			          album,publisher,description, (
					SELECT count(*)
					  FROM sound_tags
					 WHERE sound = s.id
				) AS numtags
			     FROM sounds s
		"""
		initialize()
		connection!!.createStatement().use {
			var rsltSet = it.executeQuery(sql)

			while (rsltSet.next()) {
				with (rsltSet) {
					retval.add(Sound(
						getInt("id"),
						getString("name"),
						getString("path"),
						getString("md5sum"),
						getString("description"),
						getString("title"),
						getString("artist"),
						getString("genre"),
						getString("album"),
						getString("publisher"),
						getInt("numtags")))
				}
			}
		}
		return(retval)
	}

	fun listVideos() : ArrayList<Video>
	{
		val retval = ArrayList<Video>()
		val sql = """
			   SELECT id,name,path,md5sum,description, (
					SELECT count(*)
					  FROM video_tags
					 WHERE video = v.id
				) AS numtags
			     FROM videos v
		"""
		initialize()
		connection!!.createStatement().use {
			var rsltSet = it.executeQuery(sql)

			while (rsltSet.next()) {
				with (rsltSet) {
					retval.add(Video(
						getInt("id"),
						getString("name"),
						getString("path"),
						getString("md5sum"),
						getString("description"),
						getInt("numtags")))
				}
			}
		}
		return(retval)
	}
}
