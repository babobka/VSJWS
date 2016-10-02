package ru.babobka.vsjws.constant;

/**
 * Created by dolgopolov.a on 30.12.15.
 */

public interface Method {

	String GET = "GET";

	String POST = "POST";

	String DELETE = "DELETE";

	String PUT = "PUT";

	String HEAD = "HEAD";

	String PATCH = "PATCH";

	String[] ARRAY = { GET, POST, DELETE, PUT, HEAD, PATCH };

	static boolean isValidMethod(String inputMethod) {
		for (String method : ARRAY) {
			if (inputMethod.equals(method)) {
				return true;
			}
		}
		return false;
	}

}
