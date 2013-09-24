// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.domain;

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
public class UtilTest
{
	private static final Logger logger = LoggerFactory.getLogger(
		UtilTest.class);

	private String html = null;

	public UtilTest(String html)
	{
		this.html = html;
	}

	@Parameterized.Parameters
	public static Collection html()
	{
		return Arrays.asList(new Object[][] {
			{ new StringBuilder()
			.append("<html>\n")
			.append("<head>\n")
			.append("\t<script type=\"text/javascript\" ")
			.append("src=\"http://lair.org/js/jquery.min.js\">\n")
			.append("\t<title>malicious</title>\n")
			.append("</head>\n")
			.append("<body>\n")
			.append("\t<script type=\"text/javascript\">\n")
			.append("\t\t$(function ()\n")
			.append("\t\t{\n")
			.append("\t\t\texterminate();\n")
			.append("\t\t});\n")
			.append("\t</script>\n")
			.append("</body>\n")
			.append("</html>\n").toString()
			},
			{ "<script>$(function () { exterminate })</script>" },
			{ "<SCRIPT>$(function () { exterminate })</ScRiPt>" },
			{ "< script>$(FUNCTION () { exterminate })</ ScRiPt >" }
		});
	}

	@Test
	public void sanitizePostedHTMLTest1() throws Exception
	{
		String rslt = Util.sanitizePostedHTML(html);
		logger.info("HTML:\n{}\n", rslt);
		assertTrue(rslt.toLowerCase().indexOf("<script") == -1);
	}
}
