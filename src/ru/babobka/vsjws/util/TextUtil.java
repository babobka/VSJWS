package ru.babobka.vsjws.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by dolgopolov.a on 30.12.15.
 */
public class TextUtil {

	private TextUtil() {
	}

	public static String getStringFromException(Exception ex) {
		StringWriter errors = new StringWriter();
		ex.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}

}
