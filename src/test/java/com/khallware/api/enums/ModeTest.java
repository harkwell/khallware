// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.enums;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertTrue;

public class ModeTest
{
	private static final Logger logger = LoggerFactory.getLogger(
		ModeTest.class);

	private int pattern = 0;

	@Before
	public void init()
	{
		pattern = Mode.userRead.mask();
	}

	@Test
	public void contentTest() throws Exception
	{
		String[] who = new String[] { "user", "group", "other" };
		String[] act = new String[] { "Read", "Write", "Exec" };

		for (String whoToken : who) {
			for (String actToken : act) {
				Mode.valueOf(whoToken+actToken);
			}
		}
	}

	@Test
	public void enumTest()
	{
		assertTrue(Mode.class.isEnum());
	}

	@Test
	public void normalizeTest()
	{
		logger.info("777 normalized is {}", Mode.toNormalized(777));
		assertTrue(511 == Mode.toNormalized(777));
	}

	@Test
	public void naturalTest()
	{
		logger.info("1023 naturalized is {}", Mode.toNatural(777));
		assertTrue(777 == Mode.toNatural(1023));
	}

	@Test
	public void maskTest()
	{
		for (Mode mode : Mode.values()) {
			logger.debug("mode {} mask = {}", mode, mode.mask());
		}
		assertTrue(Mode.userRead.mask() == 256);
	}

	@Test
	public void matchesTest()
	{
		assertTrue(Mode.matches(Mode.userRead, 400));
	}

	@Test
	public void parseTest()
	{
		List<Mode> modes = Mode.parse(777);
		logger.info("777 modes: {}", modes);

		for (Mode mode : Mode.values()) {
			if (mode == null) {
				continue;
			}
			assertTrue(modes.contains(mode));
		}
	}

	@Test
	public void formatTest()
	{
		List<Mode> modes = new ArrayList<>();
		int rslt = 0;

		for (Mode mode : Mode.values()) {
			modes.add(mode);
		}
		rslt = Mode.format(modes);
		logger.info("all mode values set (rwxrwxrwx) is int {}", rslt);
		assertTrue(777 == rslt);
	}
}
