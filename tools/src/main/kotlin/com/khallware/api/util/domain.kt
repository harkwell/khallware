// Copyright Kevin D.Hall 2018

package com.khallware.api.util

import java.math.BigDecimal

data class Tag(val id: Int, val name: String)
{
}

data class Bookmark(val id: Int, val name: String, val url: String,
		val numtags: Int)
{
}

data class Location(val id: Int, val name: String, val lat: BigDecimal,
		val lon: BigDecimal, val address: String, val desc: String,
		val numtags: Int)
{
}

data class Contact(val id: Int, val name: String, val uid: String,
		val email: String, val phone: String, val title: String,
		val address: String, val organization: String,
		val vcard: String, val desc: String, val numtags: Int)
{
}

data class Event(val id: Int, val name: String, val uid: String,
		val duration: Int, val start: Int, val end: Int,
		val ics: String, val desc: String, val numtags: Int)
{
}

data class Photo(val id: Int, val name: String, val path: String,
		val md5sum: String, val desc: String, val numtags: Int)
{
}

data class FileItem(val id: Int, val name: String, val ext: String,
		val md5sum: String, val desc: String, val mime: String,
		val path: String, val numtags: Int)
{
}

data class Sound(val id: Int, val name: String, val path: String,
		val md5sum: String, val desc: String, val title: String,
		val artist: String, val genre: String, val album: String,
		val publisher: String, val numtags: Int)
{
}

data class Video(val id: Int, val name: String, val path: String,
		val md5sum: String, val desc: String, val numtags: Int)
{
}
