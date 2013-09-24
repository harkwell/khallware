// Copyright Kevin D.Hall 2014-2015

package com.khallware.api;

import com.khallware.api.validation.CompleteEvent;
import com.khallware.api.domain.Credentials;
import com.khallware.api.domain.Event;
import biweekly.component.VEvent;
import biweekly.ICalendar;
import biweekly.Biweekly;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Make Event instances.
 *
 * @author khall
 */
public class EventFactory
{
	private static final Logger logger = LoggerFactory.getLogger(
		EventFactory.class);

	/**
	 * Create a list of events given a textual representation of an
	 * ICalendar list.
	 *
	 * @param text - serialized ICalendar list
	 * @return List<Event> - list of events
	 */
	public static List<Event> make(String text) throws APIException
	{
		List<Event> retval = new ArrayList<>();
		try {
			for (ICalendar ical : Biweekly.parse(text).all()) {
				retval.addAll(make(ical));
			}
		}
		catch (RuntimeException e) {
			logger.trace(""+e, e);
			logger.warn(""+e);
			throw new APIException(e);
		}
		return(retval);
	}

	/**
	 * Create a list of events given an ICalendar instance.
	 *
	 * @param ical - ICalendar instance.
	 * @return List<Event> - list of events
	 */
	public static List<Event> make(ICalendar ical) throws APIException
	{
		List<Event> retval = new ArrayList<>();
		CompleteEvent validator = new CompleteEvent();
		Event event = null;

		for (VEvent vEvent : ical.getEvents()) {
			if (validator.isValid((event = make(vEvent)))) {
				retval.add(event);
			}
			else {
				logger.warn("invalid ({})", event);
			}
		}
		return(retval);
	}

	/**
	 * Create an Event adapted from a VEvent.
	 *
	 * @param vEvent - VEvent instance.
	 * @return Event - list of events
	 */
	public static Event make(VEvent vEvent) throws APIException
	{
		Event retval = null;
		try {
			Date now = new Date();
			ICalendar ical = new ICalendar();
			// these are optional values
			String dateStart = (vEvent.getDateStart() != null)
				? ""+vEvent.getDateStart().getValue()
				: null;
			String dateEnd = (vEvent.getDateEnd() != null)
				? ""+vEvent.getDateEnd().getValue()
				: null;
			String summary = (vEvent.getSummary() != null)
				? vEvent.getSummary().getValue()
				: null;
			String uid = (vEvent.getUid() != null)
				? vEvent.getUid().getValue()
				: null;
			long dur = (vEvent.getDuration() != null)
				? vEvent.getDuration().getValue().toMillis()
				: 0;
			ical.addEvent(vEvent);
			retval = Event.builder()
				.name(summary)
				.duration(dur)
				.modified(now)
				.created(now)
				.start(dateStart)
				.end(dateEnd)
				.user(Credentials.UNKNOWN)
				.ics(Biweekly.write(ical).go())
				.description(summary)
				.uid(uid)
				.build();
			logger.info("created event ("+retval+")");
		}
		catch (Exception e) {
			logger.trace(""+e, e);
			logger.warn(""+e);
			throw new APIException(e);
		}
		return(retval);
	}
}
