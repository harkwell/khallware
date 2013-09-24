// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.validation;

import com.khallware.api.domain.APIEntity;
import java.util.Collection;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class CompletenessTest
{
	private static final Logger logger = LoggerFactory.getLogger(
		CompletenessTest.class);

	private String html = null;
	private TestAPIEntity entity = null;
	private Completeness validator = null;
	private Boolean expectedResult = null;

	public static class TestAPIEntity extends APIEntity
	{
		protected String rfld1 = null;
		protected String rfld2 = null;
		protected String rfld3 = null;

		public TestAPIEntity(String rfld1, String rfld2, String rfld3)
		{
			this.rfld1 = rfld1;
			this.rfld2 = rfld2;
			this.rfld3 = rfld3;
		}
	}

	public CompletenessTest(final String[] requiredFields,
			TestAPIEntity entity, boolean expectedResult)
	{
		validator = new Completeness() {
			@Override
			public List<String> onGetFieldNames(APIEntity e)
			{
				return(Arrays.asList(requiredFields));
			}
		};
		this.expectedResult = expectedResult;
		this.entity = entity;
	}

	@Parameterized.Parameters
	public static Collection html()
	{
		TestAPIEntity hasAll = new TestAPIEntity("go", "go", "go");
		TestAPIEntity hasOne = new TestAPIEntity("go", null, null);
		TestAPIEntity hasTwo = new TestAPIEntity(null, "go", null);
		TestAPIEntity hasThree = new TestAPIEntity(null, null, "go");
		TestAPIEntity hasOneTwo = new TestAPIEntity("go", "go", null);
		TestAPIEntity hasTwoThree = new TestAPIEntity(null, "go", "go");
		TestAPIEntity hasOneThree = new TestAPIEntity("go", null, "go");

		return Arrays.asList(new Object[][] {
			{ new String[] { "rfld1" }, hasOne, true },
			{ new String[] { "rfld2" }, hasTwo, true },
			{ new String[] { "rfld3" }, hasThree, true },
			{ new String[] { "rfld1","rfld2" }, hasOneTwo, true },
			{ new String[] { "rfld2","rfld3" }, hasTwoThree, true },
			{ new String[] { "rfld1","rfld3" }, hasOneThree, true },
			{ new String[] { "rfld1","rfld2","rfld3"},hasAll,true },
			{ new String[] { "rfld2" }, hasOne, false },
			{ new String[] { "rfld1" }, hasTwo, false },
			{ new String[] { "rfld1" }, hasThree, false },
			{ new String[] { "rfld1","rfld3" }, hasOneTwo, false },
			{ new String[] { "rfld1","rfld2" }, hasTwoThree,false },
			{ new String[] { "rfld2" }, hasOneThree, false },
			{ new String[] { "rfld1","rfld3" }, hasAll, true },
			{ new String[] { }, hasOne, true },
			{ new String[] { }, hasTwo, true },
			{ new String[] { }, hasThree, true },
			{ new String[] { }, hasOneTwo, true },
			{ new String[] { }, hasTwoThree, true },
			{ new String[] { }, hasOneThree, true },
			{ new String[] { }, hasAll, true }
		});
	}

	@Test
	public void sanitizePostedHTMLTest1() throws Exception
	{
		assertTrue((validator.isValid(entity) == expectedResult));
	}
}
