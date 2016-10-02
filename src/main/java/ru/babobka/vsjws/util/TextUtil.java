package ru.babobka.vsjws.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by dolgopolov.a on 30.12.15.
 */
public interface TextUtil {

	public static String getStringFromException(Exception ex) {
		StringWriter errors = new StringWriter();
		ex.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}

	public static String beautifyServerName(String serverName, int port) {
		return "'" + serverName + "':" + port;
	}

	public static int tryParseInt(String value) {
		return tryParseInt(value, 0);
	}

	public static int tryParseInt(String value, int defaultValue) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}

}
