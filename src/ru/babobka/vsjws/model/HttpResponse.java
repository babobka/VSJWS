package ru.babobka.vsjws.model;

import ru.babobka.vsjws.constant.ContentType;
import ru.babobka.vsjws.util.TextUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.tika.Tika;

/**
 * Created by dolgopolov.a on 30.12.15.
 */
public class HttpResponse {

	public enum ResponseCode {

		OK("200 Ok"),

		MOVED_PERMANENTLY("301 Moved permanently"),

		MOVED_TEMPORARILY("302 Moved temporarily"),

		NOT_FOUND("404 Not found"),

		UNAUTHORIZED("401 Unauthorized"),

		METHOD_NOT_ALLOWED("405 Method not allowed"),

		BAD_REQUEST("400 Bad request"),

		FORBIDDEN("403 Forbidden"),

		INTERNAL_SERVER_ERROR("500 Internal server error"),

		NOT_IMPLEMENTED("501 Not implemented"),

		SERVICE_UNAVAILABLE("503 Service unavailable"),

		REQUEST_TIMEOUT("408 Request Timeout");

		private final String text;

		private ResponseCode(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public static final Charset MAIN_ENCODING = Charset.forName("UTF-8");

	public static final HttpResponse NOT_FOUND_RESPONSE = textResponse(
			ResponseCode.NOT_FOUND.toString(), ResponseCode.NOT_FOUND,
			ContentType.PLAIN);

	public static final HttpResponse NOT_IMPLEMENTED_RESPONSE = textResponse(
			ResponseCode.NOT_IMPLEMENTED.toString(),
			ResponseCode.NOT_IMPLEMENTED, ContentType.PLAIN);

	private static final Tika tika = new Tika();

	private final Map<String, String> cookies = new HashMap<>();

	private final ResponseCode responseCode;

	private final String contentType;

	private final byte[] content;

	private final File file;

	private final long contentLength;

	public HttpResponse(ResponseCode code, String contentType, byte[] content,
			File file, long contentLength) {
		super();
		this.responseCode = code;
		this.contentType = contentType;
		this.content = content;
		this.file = file;
		this.contentLength = contentLength;
	}

	/*
	 * public HttpResponse(byte[] content, String responseCode, String
	 * contentType) { this.responseCode = responseCode; this.contentType =
	 * contentType; this.content = content; this.contentLength = content.length;
	 * this.file = null;
	 * 
	 * }
	 */

	public static HttpResponse rawResponse(byte[] content, ResponseCode code,
			String contentType) {
		return new HttpResponse(code, contentType, content, null,
				content.length);
	}

	public static HttpResponse rawResponse(byte[] content, String contentType) {
		return rawResponse(content, ResponseCode.OK, contentType);
	}

	/*
	 * public HttpResponse(File file, String responseCode) throws IOException {
	 * this.responseCode = responseCode; if (file.exists() && file.isFile()) {
	 * this.contentType = Files.probeContentType(file.toPath()); this.content =
	 * null; this.file = file; this.contentLength = file.length(); } else {
	 * throw new FileNotFoundException(); }
	 * 
	 * }
	 */

	public static HttpResponse fileResponse(File file, ResponseCode code)
			throws IOException {
		if (file.exists() && file.isFile()) {
			return new HttpResponse(code, tika.detect(file), null, file,
					file.length());
		} else {
			throw new FileNotFoundException();
		}

	}

	public static HttpResponse fileResponse(File file) throws IOException {
		return fileResponse(file, ResponseCode.OK);

	}

	/*
	 * public HttpResponse(String content, String responseCode, String
	 * contentType) { this.responseCode = responseCode; this.contentType =
	 * contentType; this.content = content.getBytes(MAIN_ENCODING);
	 * this.contentLength = content.getBytes().length; this.file = null; }
	 */

	public static HttpResponse textResponse(String content, ResponseCode code,
			String contentType) {
		byte[] bytes = content.getBytes(MAIN_ENCODING);
		return new HttpResponse(code, contentType, bytes, null, bytes.length);
	}

	public static HttpResponse ok() {
		return textResponse("Ok", ResponseCode.OK, ContentType.PLAIN);
	}

	public static HttpResponse exceptionResponse(Exception e, ResponseCode code) {
		return textResponse(TextUtil.getStringFromException(e), code,
				ContentType.PLAIN);
	}

	public static HttpResponse exceptionResponse(Exception e) {
		return exceptionResponse(e, ResponseCode.INTERNAL_SERVER_ERROR);
	}

	public static HttpResponse textResponse(String content, String contentType) {
		return textResponse(content, ResponseCode.OK, contentType);
	}

	public void addCookie(String key, String value) {
		cookies.put(key, value);
	}

	public Map<String, String> getHttpCookieHeaders() {
		HashMap<String, String> headers = new HashMap<>();
		for (Map.Entry<String, String> cookie : cookies.entrySet()) {
			headers.put("Set-Cookie:",
					cookie.getKey() + "=" + cookie.getValue());
		}
		return headers;
	}

	public ResponseCode getResponseCode() {
		return responseCode;
	}

	public String getContentType() {
		return contentType;
	}

	public byte[] getContent() {
		return content;
	}

	public long getContentLength() {
		return contentLength;
	}

	public File getFile() {
		return file;
	}

}
