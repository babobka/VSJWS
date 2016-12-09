package ru.babobka.vsjws.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;

/**
 * Created by dolgopolov.a on 29.12.15.
 */
public interface HttpUtil {

	public static void writeResponse(OutputStream os, HttpResponse response, boolean noContent) throws IOException {
		if (response != null) {
			StringBuilder headerBuilder = new StringBuilder(
					HttpRequest.PROTOCOL + " " + response.getResponseCode() + "\n");
			Map<String, String> headers = new LinkedHashMap<>();
			headers.put(HttpResponse.RestrictedHeader.SERVER + ":", "vsjws");
			headers.put(HttpResponse.RestrictedHeader.CONTENT_TYPE + ":", response.getContentType());
			headers.put(HttpResponse.RestrictedHeader.CONTENT_LENGTH + ":",
					String.valueOf(response.getContentLength()));
			headers.put(HttpResponse.RestrictedHeader.CONNECTION + ":", "close");
			headers.putAll(response.getHttpCookieHeaders());
			headers.putAll(response.getOtherHeaders());
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				headerBuilder.append(entry.getKey());
				headerBuilder.append(" ");
				headerBuilder.append(entry.getValue());
				headerBuilder.append("\r\n");
			}

			headerBuilder.append("\r\n");
			os.write(headerBuilder.toString().getBytes(HttpResponse.MAIN_ENCODING));
			if (!noContent) {
				if (response.getFile() != null) {
					byte[] buf = new byte[8192];
					int c;
					try (InputStream is = new FileInputStream(response.getFile())) {
						while ((c = is.read(buf, 0, buf.length)) > 0) {
							os.write(buf, 0, c);
							os.flush();
						}
					}
				} else {
					os.write(response.getContent());
				}
			}
		}
		os.flush();

	}

	public static Map<String, String> getUriParams(String uri) {
		String[] uriArray = uri.split("\\?");
		if (uriArray.length > 1) {
			return getParams(uriArray[1]);
		} else {
			return new HashMap<>();
		}
	}

	public static String getRawHttpRequest(InputStream is) throws IOException {

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line;
			int contentLength = -1;
			while ((line = br.readLine()) != null) {
				if (line.startsWith(HttpRequest.CONTENT_LENGTH_HEADER)) {
					contentLength = Integer.parseInt(getHeaderValue(line));
				}
				if (line.isEmpty()) {
					sb.append("\n");
					if (contentLength != -1) {
						sb.append(HttpUtil.readBody(contentLength, br));
					}

					break;
				}
				sb.append(line);
			}
			return sb.toString();
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

	public static String getHeaderValue(String headerLine) {
		return headerLine.substring(headerLine.indexOf(':') + 2, headerLine.length());
	}

	public static String cleanUri(String uri) {
		return uri.split("\\?")[0];

	}

	public static String readBody(int contentLength, BufferedReader br) throws IOException {
		StringBuilder body = new StringBuilder();
		for (int i = 0; i < contentLength; i++) {
			body.append((char) br.read());
		}
		return new String(body.toString().getBytes(), HttpResponse.MAIN_ENCODING);
	}

	public static Map<String, String> getCookies(String cookiesLine) {
		Map<String, String> cookies = new HashMap<>();
		if (!cookiesLine.isEmpty()) {
			String[] cookiesArray = cookiesLine.substring(0, cookiesLine.length()).split("; ");
			for (int i = 0; i < cookiesArray.length; i++) {
				String[] cookie = cookiesArray[i].split("=");
				cookies.put(cookie[0], cookie[1]);
			}
		}
		return cookies;
	}

	public static String generateSessionId() {
		return String.valueOf((long) (Math.random() * Long.MAX_VALUE));
	}

	public static Map<String, String> getParams(String paramText) {
		Map<String, String> params = new HashMap<>();
		if (paramText != null && paramText.length() > 0) {
			String[] paramsArray = paramText.split("&");
			for (int i = 0; i < paramsArray.length; i++) {
				String[] keyValue = paramsArray[i].split("=");
				if (keyValue.length > 1) {
					params.put(keyValue[0], keyValue[1]);
				}
			}
		}
		return params;
	}
}
