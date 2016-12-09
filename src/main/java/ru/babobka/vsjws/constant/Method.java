package ru.babobka.vsjws.constant;

/**
 * Created by dolgopolov.a on 30.12.15.
 */

public class Method {

	public static final String GET = "GET";

	public static final String POST = "POST";

	public static final String DELETE = "DELETE";

	public static final String PUT = "PUT";

	public static final String HEAD = "HEAD";

	public static final String PATCH = "PATCH";

	private static final String[] ARRAY = { GET, POST, DELETE, PUT, HEAD, PATCH };

	public static boolean isValidMethod(String inputMethod) {
		for (String method : ARRAY) {
			if (inputMethod.equals(method)) {
				return true;
			}
		}
		return false;
	}

}
