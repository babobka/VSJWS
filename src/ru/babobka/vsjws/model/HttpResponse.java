package ru.babobka.vsjws.model;

import ru.babobka.vsjws.constant.ContentType;
import ru.babobka.vsjws.constant.RegularExpressions;
import ru.babobka.vsjws.util.TextUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.tika.Tika;

/**
 * Created by dolgopolov.a on 30.12.15.
 */
public class HttpResponse {

	public enum RestrictedHeader {

		SERVER("Server"), CONTENT_TYPE("Content-Type"), CONTENT_LENGTH("Content-Length"), CONNECTION("Connection");

		private final String text;

		private RestrictedHeader(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public enum ResponseCode {

		OK("200 Ok"), ACCEPTED("202 Accepted"),

		MOVED_PERMANENTLY("301 Moved permanently"),

		MOVED_TEMPORARILY("302 Moved temporarily"),

		SEE_OTHER("303 See other"),

		NOT_FOUND("404 Not found"),

		UNAUTHORIZED("401 Unauthorized"),

		METHOD_NOT_ALLOWED("405 Method not allowed"),

		BAD_REQUEST("400 Bad request"),

		FORBIDDEN("403 Forbidden"),

		INTERNAL_SERVER_ERROR("500 Internal server error"),

		NOT_IMPLEMENTED("501 Not implemented"),

		SERVICE_UNAVAILABLE("503 Service unavailable"),

		LENGTH_REQUIRED("411 Length required"),

		HTTP_VERSION_NOT_SUPPORTED("505 HTTP version not supported"),

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

	public static final HttpResponse NOT_FOUND_RESPONSE = textResponse(ResponseCode.NOT_FOUND.toString(),
			ResponseCode.NOT_FOUND, ContentType.PLAIN);

	public static final HttpResponse LENGTH_REQUIRED_RESPONSE = textResponse(ResponseCode.LENGTH_REQUIRED.toString(),
			ResponseCode.LENGTH_REQUIRED, ContentType.PLAIN);

	public static final HttpResponse NOT_IMPLEMENTED_RESPONSE = textResponse(ResponseCode.NOT_IMPLEMENTED.toString(),
			ResponseCode.NOT_IMPLEMENTED, ContentType.PLAIN);

	private static final Tika tika = new Tika();

	private final Map<String, String> otherHeaders = new LinkedHashMap<>();

	private final Map<String, String> cookies = new HashMap<>();

	private final ResponseCode responseCode;

	private final String contentType;

	private final byte[] content;

	private final File file;

	private final long contentLength;

	public HttpResponse addHeader(String key, String value) {

		for (RestrictedHeader header : RestrictedHeader.values()) {
			if (header.toString().equals(key)) {
				throw new IllegalArgumentException(
						"You can not manually specify '" + key + "' header. It is restricted.");
			}
		}
		otherHeaders.put(key, value);
		return this;
	}

	public HttpResponse addHeader(String key, long value) {
		return addHeader(key, String.valueOf(value));
	}

	public HttpResponse(ResponseCode code, String contentType, byte[] content, File file, long contentLength) {
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

	public static HttpResponse rawResponse(byte[] content, ResponseCode code, String contentType) {
		return new HttpResponse(code, contentType, content, null, content.length);
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

	public static HttpResponse fileResponse(File file, ResponseCode code) throws IOException {
		if (file.exists() && file.isFile()) {
			return new HttpResponse(code, tika.detect(file), null, file, file.length());
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

	public static HttpResponse redirectResponse(String url) {
		String localUrl = url;
		if (!localUrl.startsWith("http")) {
			localUrl = "http://" + localUrl;
		}
		if (url.matches(RegularExpressions.URL_PATTERN)) {
			return textResponse("Redirection", ResponseCode.OK, ContentType.PLAIN).addHeader("Location", localUrl);
		} else {
			throw new IllegalArgumentException("URL '" + url + "' is not valid");
		}
	}

	public static HttpResponse textResponse(String content) {
		return textResponse(content, ResponseCode.OK);
	}

	public static HttpResponse textResponse(String content, ResponseCode code) {
		return textResponse(content, code, ContentType.PLAIN);
	}

	public static HttpResponse textResponse(String content, ResponseCode code, String contentType) {
		byte[] bytes = content.getBytes(MAIN_ENCODING);
		return new HttpResponse(code, contentType, bytes, null, bytes.length);
	}

	public static HttpResponse ok() {
		return textResponse("Ok", ResponseCode.OK, ContentType.PLAIN);
	}

	public static HttpResponse exceptionResponse(Exception e, ResponseCode code) {
		return textResponse(TextUtil.getStringFromException(e), code, ContentType.PLAIN);
	}

	public static HttpResponse exceptionResponse(Exception e) {
		return exceptionResponse(e, ResponseCode.INTERNAL_SERVER_ERROR);
	}

	public static HttpResponse textResponse(String content, String contentType) {
		return textResponse(content, ResponseCode.OK, contentType);
	}

	public HttpResponse addCookie(String key, String value) {
		cookies.put(key, value);
		return this;
	}

	public Map<String, String> getHttpCookieHeaders() {
		HashMap<String, String> headers = new HashMap<>();
		for (Map.Entry<String, String> cookie : cookies.entrySet()) {
			headers.put("Set-Cookie:", cookie.getKey() + "=" + cookie.getValue());
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

	public Map<String, String> getOtherHeaders() {
		return otherHeaders;
	}

}
