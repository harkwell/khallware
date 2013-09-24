// Copyright Kevin D.Hall 2014-2015

package com.khallware.api;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertTrue;

public class UtilTest
{
	private static final Logger logger = LoggerFactory.getLogger(
		UtilTest.class);

	@Test
	public void hashTest1() throws Exception
	{
		String val = "robin ran away, ha ha..., brave brave sir robin";
		String rslt = Util.hash(val);
		logger.info(Util.DIGEST_TYPE+" hash({}) = \"{}\"", val, rslt);
		assertTrue(("1f1868f73fdefe3eff210efdd1e2bba90de3bae50144938a50"
			+"a00b52c3a57820").equals(rslt));
	}
}
