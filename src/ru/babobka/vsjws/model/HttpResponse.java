package ru.babobka.vsjws.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tika.Tika;

/**
 * Created by dolgopolov.a on 30.12.15.
 */

import org.json.JSONObject;

import ru.babobka.vsjws.constant.ContentType;
import ru.babobka.vsjws.constant.RegularExpressions;
import ru.babobka.vsjws.util.TextUtil;

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
		if (key.endsWith(":")) {
			otherHeaders.put(key, value);
		} else {
			otherHeaders.put(key + ":", value);
		}

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

	public static HttpResponse rawResponse(byte[] content, ResponseCode code, String contentType) {
		return new HttpResponse(code, contentType, content, null, content.length);
	}

	public static HttpResponse rawResponse(byte[] content, String contentType) {
		return rawResponse(content, ResponseCode.OK, contentType);
	}

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

	public static HttpResponse redirectResponse(String url) {
		String localUrl = url;
		if (!localUrl.startsWith("http")) {
			localUrl = "http://" + localUrl;
		}
		if (url.matches(RegularExpressions.URL_PATTERN)) {
			return textResponse("Redirection", ResponseCode.SEE_OTHER, ContentType.PLAIN).addHeader("Location",
					localUrl);
		} else {
			throw new IllegalArgumentException("URL '" + url + "' is not valid");
		}
	}

	public static HttpResponse jsonResponse(JSONObject json, ResponseCode code) {
		return textResponse(json.toString(), code, ContentType.JSON);
	}

	public static HttpResponse jsonResponse(JSONObject json) {
		return jsonResponse(json.toString(), ResponseCode.OK);
	}

	public static HttpResponse jsonResponse(String json, ResponseCode code) {
		return jsonResponse(new JSONObject(json), code);
	}

	public static HttpResponse jsonResponse(String json) {
		return jsonResponse(new JSONObject(json));
	}

	public static HttpResponse xmlResponse(String xml, ResponseCode code) {
		return textResponse(xml, code, ContentType.XML);
	}

	public static HttpResponse xmlResponse(String xml) {
		return xmlResponse(xml, ResponseCode.OK);
	}

	public static HttpResponse xsltResponse(String xml, StreamSource xslSource) throws IOException {
		return xsltResponse(xml, xslSource, ResponseCode.OK);
	}

	public static HttpResponse xsltResponse(String xml, StreamSource xslSource, ResponseCode code) throws IOException {
		StringReader reader = null;
		StringWriter writer = null;
		try {
			reader = new StringReader(xml);
			writer = new StringWriter();
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer(xslSource);
			transformer.transform(new StreamSource(reader), new StreamResult(writer));
			String html = writer.toString();
			return HttpResponse.textResponse(html, code, ContentType.HTML);
		} catch (TransformerException e) {
			throw new IOException(e);
		} finally {
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.close();
			}
		}

	}

	public static HttpResponse textResponse(String content) {
		return textResponse(content, ResponseCode.OK);
	}

	public static HttpResponse textResponse(Object content) {
		return textResponse(content.toString());
	}

	public static HttpResponse htmlResponse(String content, ResponseCode code) {
		return textResponse(content, code, ContentType.HTML);
	}

	public static HttpResponse htmlResponse(String content) {
		return textResponse(content, ResponseCode.OK, ContentType.HTML);
	}

	public static HttpResponse textResponse(String content, ResponseCode code) {
		return textResponse(content, code, ContentType.PLAIN);
	}

	public static HttpResponse textResponse(Object content, ResponseCode code) {
		return textResponse(content.toString(), code);
	}

	public static HttpResponse textResponse(String content, ResponseCode code, String contentType) {
		byte[] bytes = content.getBytes(MAIN_ENCODING);
		return new HttpResponse(code, contentType, bytes, null, bytes.length);
	}

	public static HttpResponse textResponse(Object content, ResponseCode code, String contentType) {

		return textResponse(content.toString(), code, contentType);
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
