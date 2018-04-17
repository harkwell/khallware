// Copyright Kevin D.Hall 2014-2018

package com.khallware.api.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * Mode.  This enum represents a set of permissions that limit the operations
 * on entities.
 *
 * @author khall
 */
public enum Mode
{
	userRead(0b100000000),
	userWrite(0b010000000),
	userExec(0b001000000),
	groupRead(0b000100000),
	groupWrite(0b000010000),
	groupExec(0b000001000),
	otherRead(0b000000100),
	otherWrite(0b000000010),
	otherExec(0b000000001);

	private final int mask;

	Mode(int mask)
	{
		this.mask = mask;
	}

	public int mask()
	{
		return(this.mask);
	}

	public static boolean matches(Mode mode, int pattern)
	{
		return((toNormalized(pattern) & mode.mask()) > 0);
	}

	/**
	 * Takes a form like 777 and converts it to -Mode- useful number.
	 * The string "rwxrwxrwx" is naturally represented as 777, but is not
	 * valid when used as a node value.
	 */
	public static int toNormalized(int natural)
	{
		String pattern = "000"+natural;
		String data = pattern.substring(pattern.length() - 3);
		int user = Integer.parseInt(data.substring(0, 1));
		int group = Integer.parseInt(data.substring(1, 2));
		int other = Integer.parseInt(data.substring(2, 3));
		user = (user << 6);
		group = (group << 3);
		return(user + group + other);
	}

	/**
	 * Takes a normalized form like 896 and converts it a natural val 700.
	 * The string "rwx------" is represented as 700, but masks to 896.
	 */
	public static int toNatural(int normalized)
	{
		int user  = ((0b111000000 & normalized) >>> 6);
		int group = ((0b000111000 & normalized) >>> 3);
		int other =  (0b000000111 & normalized);
		return(Integer.parseInt(""+user+""+group+""+other));
	}

	public static List<Mode> parse(int pattern)
	{
		List<Mode> retval = new ArrayList<>();

		for (Mode mode : Mode.values()) {
			if (matches(mode, pattern)) {
				retval.add(mode);
			}
		}
		return(retval);
	}

	public static int format(List<Mode> modes)
	{
		int retval = 0;

		for (Mode mode : modes) {
			retval |= mode.mask();
		}
		return(toNatural(retval));
	}
}
