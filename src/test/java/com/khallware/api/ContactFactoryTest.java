// Copyright Kevin D.Hall 2014-2015

package com.khallware.api;

import com.khallware.api.domain.Contact;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Date;
import java.util.Arrays;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ContactFactoryTest
{
	private static final Logger logger = LoggerFactory.getLogger(
		ContactFactoryTest.class);

	private static final String BAD_VCF = ""
		+"N:Reagan;Ronald;Mr.\n"
		+"FN:Ronald Wilson Reagan\n"
		+"ORG:Bubba Gump Shrimp Co.\n"
		+"TITLE:US President\n"
		+"PHOTO;VALUE=URL;TYPE=GIF:http://www.foo.com/my_photo.gif\n"
		+"TEL;TYPE=WORK,VOICE:(540) 555-1212\n"
		+"TEL;TYPE=HOME,VOICE:(615) 555-1212\n"
		+"ADR;TYPE=WORK:;;100 Waters Edge;Baytown;LA;30314;US\n"
		+"LABEL;TYPE=HOME:42 Plantation St.\nBaytown, LA 30314\nUS\n"
		+"EMAIL;TYPE=PREF,INTERNET:forrestgump@example.com\n";

	private String input = null;
	private int size = -1;

	public ContactFactoryTest(String input, int size)
	{
		this.input = input;
		this.size = size;
	}

	@Parameterized.Parameters
	public static Collection contacts()
	{
		String contact1 = contact("Joe Issuzu","joe.issuzu@gmail.com");
		String contact2 = contact("Phil Robertson","phil@duckcmdr.com");
		String contact3 = contact("Jason Bourne","bourne@amnesia.org");
		String contact4 = contact("2008-04-24T19:52:43Z",
			"George Washington", "United States", "President",
			"(540) 555-1212", "george.washington@whitehouse.gov",
			"1600 Pennsylvania Ave;Washington;DC;",
			"US President");
		String bad1 = contact(null, null);
		String bad2 = contact(null, "no body");
		return Arrays.asList(new Object[][] {
			{ contact1, 1 },
			{ contact2, 1 },
			{ contact3, 1 },
			{ bad1, 0 },
			{ BAD_VCF, 0 },
			{ contact1+contact2, 2 },
			{ contact1+contact3+bad1, 2 },
			{ contact1+bad2+contact3, 2 },
			{ contact1+contact2+contact3+contact4, 4 }
		});
	}

	@Test
	public void factoryTest()
	{
		try {
			List<Contact> rslt = ContactFactory.make(input);
			logger.info("input ({}), output ({})", input, rslt);
			logger.info("created {} of {} contacts", rslt.size(),
				size);
			assertTrue(rslt.size() == size);
		}
		catch (Exception e) {
			logger.error(""+e, e);
		}
	}

	protected static String contact(String name, String email)
	{
		String format = "yyyy-MM-dd'T'HH:mm:ssZ";
		return(contact(new SimpleDateFormat(format).format(new Date()),
			name, null, null, null, email, null, null));
	}

	protected static String contact(String rev, String name, String org,
			String title, String phone, String email,
			String address, String description)
	{
		StringBuilder retval = new StringBuilder()
			.append("BEGIN:VCARD\n")
			.append("VERSION:3.0\n")
			.append((name == null) ? "" : "N:"+convertFN(name)+"\n")
			.append((name == null) ? "" : "FN:"+name+"\n")
			.append((org == null) ? "" : "ORG:"+org+"\n")
			.append((title == null) ? "" : "TITLE:"+title+"\n")
			.append((phone == null)
				? ""
				: "TEL;TYPE=WORK,VOICE:"+phone+"\n")
			.append((address == null)
				? ""
				: "ADR;TYPE=WORK:"+address+"\n")
			.append((email == null)
				? ""
				: "EMAIL;TYPE=PREF,INTERNET:"+email+"\n")
			.append((rev == null) ? "" : "REV:"+rev+"\n")
			.append("END:VCARD\n");
		return(""+retval);
	}

	protected static String convertFN(String fn)
	{
		String retval = "";
		String[] tokens = fn.split(" ");

		if (tokens.length <= 1) {
			retval += fn;
		}
		else if (tokens.length == 2) {
			retval = tokens[1]+";"+tokens[0];
		}
		else {
			retval = tokens[tokens.length-1]+";"+tokens[0];
		}
		return(retval);
	}
}
