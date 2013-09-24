// Copyright Kevin D.Hall 2014-2015

package com.khallware.api.domain;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility functions.
 *
 * @author khall
 */
public final class Util
{
	private static final Logger logger = LoggerFactory.getLogger(
		Util.class);

	public static String sanitizePostedHTML(String html)
	{
		String retval = html;
		int flags = (Pattern.CASE_INSENSITIVE & Pattern.DOTALL);
		String[] regexArray = new String[] {
			"(?i)[<][/]*?[\\s]*?(script)", "[$][(]"
		};
		for (String regex : regexArray) {
			Pattern pattern = Pattern.compile(regex, flags);
			Matcher matcher = pattern.matcher(retval);
			retval = matcher.replaceAll("");
		}
		return(retval);
	}

	public static synchronized UUID makeUUID(APIEntity entity)
	{
		byte version = 0b00000001;
		long datetime = entity.getModified().getTime();
		long v1 = ((datetime << 32) >>> 32);
		long v2 = ((datetime << 16) >>> 32);
		long v3 = ((datetime << 1) >>> 49);
		long v4 = (long)entity.getId();
		String time_low = Long.toHexString(v1|(0b1L<<33)).substring(1);
		String time_mid = Long.toHexString(v2|(0b1L<<17)).substring(1);
		String time_high_and_version = ""+version+Long.toHexString(
			v3|(0b1L<<16)).substring(1);
		String variant_and_sequence = Long.toHexString(0xffff);
		String node = Long.toHexString(v4|(0b11L<<17)).substring(1);
		return(UUID.fromString(time_low+"-"+time_mid+"-"
			+time_high_and_version+"-"+variant_and_sequence+"-"
			+node));
	}
}
