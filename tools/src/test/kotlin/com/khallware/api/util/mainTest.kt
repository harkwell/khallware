// Copyright Kevin D.Hall 2018

package com.khallware.api.util

import org.slf4j.LoggerFactory
import kotlin.test.assertTrue
import org.junit.Test

class MainTest
{
	@Test
	fun testGetFilespecs()
	{
		val rslt = getFilespecs()
		logger.debug("filespecs: {}", rslt)
		assertTrue(rslt.size > 0)
	}

	@Test
	fun testValidUrl()
	{
		assertTrue(validUrl("https://www.google.com/"))
		assertTrue(!validUrl("htp://www.google.com/"))
		assertTrue(!validUrl("http://www.g o o g l e.com/"))
	}
}
