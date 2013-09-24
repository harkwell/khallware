// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.ctrl;

import com.khallware.api.domain.AtomEntity;
import com.khallware.api.Unauthorized;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import javax.ws.rs.core.Response;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertTrue;

public class UtilTest
{
	private static final Logger logger = LoggerFactory.getLogger(
		UtilTest.class);

	@Mock private Unauthorized mockUnauthorized;
	@Mock private AtomEntity mockAtomEntity1;
	@Mock private AtomEntity mockAtomEntity2;

	@Before
	public void init()
	{
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void failRequestTest1()
	{
		Response rslt = Util.failRequest(mockUnauthorized);
		assertTrue(rslt.getStatus() == 401);
		logger.trace("HEADERS:\n{}", rslt.getHeaders());
	}

	@Test
	public void failRequestTest2()
	{
		Response rslt = Util.failRequest(null);
		assertTrue(rslt.getStatus() == 400);
		logger.trace("HEADERS:\n{}", rslt.getHeaders());
	}

	@Test
	public void lastPeriodTest1()
	{
		List<AtomEntity> items = new ArrayList<>();
		List<AtomEntity> rslt = null;
		Date now = new Date();
		when(mockAtomEntity1.getModified()).thenReturn(now);
		when(mockAtomEntity2.getModified()).thenReturn(now);
		items.add(mockAtomEntity1);
		items.add(mockAtomEntity2);
		rslt = Util.lastPeriod(items, "http://foo.com/apis/",null,now);
		assertTrue(rslt.size() == 0);
	}
}
