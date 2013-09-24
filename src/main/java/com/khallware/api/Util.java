// Copyright Kevin D.Hall 2014-2015

package com.khallware.api;

import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.math.BigInteger;
import java.nio.file.Files;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility functions.
 *
 * @author khall
 */
public final class Util
{
	public static final String DIGEST_TYPE = "SHA-256";
	private static final Logger logger = LoggerFactory.getLogger(
		Util.class);

	public static String hash(String plain) throws NoSuchAlgorithmException
	{
		StringBuffer retval = new StringBuffer();
		MessageDigest md = MessageDigest.getInstance(DIGEST_TYPE);
		md.update(plain.getBytes());

		for (byte b : md.digest()) {
			String hex = Integer.toHexString(0xff & b);
			retval.append((hex.length() == 1) ? "0" : "");
			retval.append(hex);
		}
		return(""+retval);
	}

	public static String produceHashSum(String type, File src)
			throws Exception
	{
		String retval = null;
		MessageDigest md = MessageDigest.getInstance(type);
		byte[] hash = md.digest(Files.readAllBytes(src.toPath()));
		retval = String.format("%032x", new BigInteger(1, hash));
		logger.trace("file \"{}\" has md5sum \"{}\"", ""+src, retval);
		return(retval);
	}
}
