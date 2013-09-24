// Copyright Kevin D.Hall 2014-2015

package com.khallware.api;

import com.khallware.api.domain.Event;
import java.util.Collection;
import java.util.UUID;
import java.util.List;
import java.util.Arrays;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class EventFactoryTest
{
	private static final Logger logger = LoggerFactory.getLogger(
		EventFactoryTest.class);

	private static final String BAD_ICS = ""
		+"BEGIN:VCALENDAR\n"
		+"VERSION:2.0\n"
		+"PRODID:-//hacksw/handcal//NONSGML v1.0//EN\n"
		+"DTSTAMP:19970714T170000Z\n"
		+"ORGANIZER;CN=John Doe:MAILTO:john.doe@example.com\n"
		+"DTSTART:19970714T170000Z\n"
		+"DTEND:19970715T035959Z\n"
		+"SUMMARY:Bastille Day Party\n"
		+"END:VEVENT\n"
		+"END:VCALENDAR\n";

	private String input = null;
	private int size = -1;

	public EventFactoryTest(String input, int size)
	{
		this.input = input;
		this.size = size;
	}

	@Parameterized.Parameters
	public static Collection calendars()
	{
		String event1[] = { "uid1@example.com", "19970714T170000Z",
			"Mon Jul 14 22:59:59 CDT 1997", "John Doe",
			"john.doe@example.com", "Bastille Day" };
		String event2[] = { "uid2@example.com", "20141127T000000Z",
			"20141127T000000Z", "Dr. Who", "dw@tardis.com",
			"Thanksgiving" };
		String event3[] = { ""+UUID.randomUUID(), "20141031T000000Z",
			"20141031T000000Z", "spook", "spook@halloween.com",
			"Halloween" };
		String bad1[] = { null, null, null, null, null, null };
		String bad2[] = { ""+UUID.randomUUID(), null, null, null,
			"santa@northpole.com", null };
		return Arrays.asList(new Object[][] {
			{ calendar(event1), 1 },
			{ calendar(event2), 1 },
			{ calendar(event3), 1 },
			{ calendar(bad1), 0 },
			{ BAD_ICS, 0 },
			{ calendar(event1, event2), 2 },
			{ calendar(event1, event3, bad1), 2 },
			{ calendar(event1, bad2, event3), 2 },
			{ calendar(event1, event2, event3), 3 }
		});
	}

	@Test
	public void factoryTest()
	{
		try {
			List<Event> rslt = EventFactory.make(input);
			logger.info("input ({}), output ({})", input, rslt);
			logger.info("created {} of {} events",rslt.size(),size);
			assertTrue(rslt.size() == size);
		}
		catch (Exception e) {
			logger.error(""+e, e);
		}
	}

	protected static String calendar(String[]... events)
	{
		StringBuilder retval = new StringBuilder()
			.append("BEGIN:VCALENDAR\n")
			.append("VERSION:2.0\n")
			.append("PRODID:-//hacksw/handcal//NONSGML v1.0//EN\n");

		for (String[] event : events) {
			if (event.length < 6) {
				continue;
			}
			String uuid = event[0];
			String begin = event[1];
			String end = event[2];
			String name = event[3];
			String email = event[4];
			String summary = event[5];
			retval.append(""
				+"BEGIN:VEVENT\n"
				+((uuid == null)
					? ""
					: "UID:"+uuid+"\n")
				+"DTSTAMP:19970714T170000Z\n"
				+((name == null || email == null)
					? ""
					:"ORGANIZER;CN="+name
						+":MAILTO:"+email+"\n")
				+((begin == null)
					? ""
					: "DTSTART:"+begin+"\n")
				+((end == null)
					? ""
					: "DTEND:"+end+"\n")
				+((summary == null)
					? ""
					: "SUMMARY:"+summary+"\n")
				+"END:VEVENT\n");
		}
		retval.append("END:VCALENDAR\n");
		return(""+retval);
	}
}
