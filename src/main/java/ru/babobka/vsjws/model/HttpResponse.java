package ru.babobka.vsjws.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import ru.babobka.vsjws.constant.ContentType;
import ru.babobka.vsjws.constant.RegularExpressions;
import ru.babobka.vsjws.util.TextUtil;

public class HttpResponse {

	private static final Gson GSON = new Gson();

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

		OK(200, "Ok"), ACCEPTED(202, "Accepted"), NO_CONTENT(204, "No content"),

		MOVED_PERMANENTLY(301, "Moved permanently"),

		MOVED_TEMPORARILY(302, "Moved temporarily"),

		SEE_OTHER(303, "See other"),

		NOT_FOUND(404, "Not found"),

		UNAUTHORIZED(401, "Unauthorized"),

		METHOD_NOT_ALLOWED(405, "Method not allowed"),

		BAD_REQUEST(400, "Bad request"),

		FORBIDDEN(403, "Forbidden"),

		INTERNAL_SERVER_ERROR(500, "Internal server error"),

		NOT_IMPLEMENTED(501, "Not implemented"),

		SERVICE_UNAVAILABLE(503, "Service unavailable"),

		LENGTH_REQUIRED(411, "Length required"),

		HTTP_VERSION_NOT_SUPPORTED(505, "HTTP version not supported"),

		REQUEST_TIMEOUT(408, "Request Timeout");

		private final String text;
		private final int code;

		private ResponseCode(int code, String text) {
			this.text = text;
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public String getText() {
			return text;
		}

		@Override
		public String toString() {
			return code + " " + text;
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
		if (content != null)
			this.content = content.clone();
		else
			this.content = null;
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

	public static HttpResponse resourceResponse(String fileName, ResponseCode code) throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
		if (is != null) {
			byte[] bytes = IOUtils.toByteArray(is);
			return new HttpResponse(code, tika.detect(bytes), bytes, null, bytes.length);
		} else {
			throw new FileNotFoundException();
		}

	}

	public static HttpResponse resourceResponse(String fileName) throws IOException {
		return resourceResponse(fileName, ResponseCode.OK);

	}

	public static HttpResponse resourceResponse(InputStream is, ResponseCode code) throws IOException {
		byte[] bytes = IOUtils.toByteArray(is);
		return new HttpResponse(code, tika.detect(bytes), bytes, null, bytes.length);

	}

	public static HttpResponse resourceResponse(InputStream is) throws IOException {
		return resourceResponse(is, ResponseCode.OK);

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

	public static HttpResponse jsonResponse(Object object) {
		return jsonResponse(object, ResponseCode.OK);
	}

	public static HttpResponse jsonResponse(Object object, ResponseCode code) {
		if (object == null) {
			throw new IllegalArgumentException("JSON object can not be null");
		} else {
			return textResponse(GSON.toJson(object), code, ContentType.JSON);
		}
	}

	public static HttpResponse jsonResponse(String json, ResponseCode code) {
		return jsonResponse(new JSONObject(json), code);
	}

	public static HttpResponse jsonResponse(String json) {
		if(json==null)
		{
			throw new IllegalArgumentException();
		}
		return jsonResponse(new JSONObject(json));
	}

	public static HttpResponse xmlResponse(String xml, ResponseCode code) {
		return textResponse(xml, code, ContentType.XML);
	}

	public static HttpResponse xmlResponse(String xml) {
		return xmlResponse(xml, ResponseCode.OK);
	}

	public static HttpResponse xsltResponse(Map<String, Serializable> map, String xslFileName) throws IOException {
		return xsltResponse(map, xslFileName, ResponseCode.OK);
	}

	public static HttpResponse xsltResponse(Map<String, Serializable> map, String xslFileName, ResponseCode code)
			throws IOException {
		XStream stream = new XStream();
		stream.registerConverter(new MapEntryConverter());
		stream.alias("root", Map.class);
		return xsltResponse(stream.toXML(map), xslFileName, code);
	}

	public static HttpResponse xsltResponse(String xml, String xslFileName) throws IOException {
		return xsltResponse(xml, xslFileName, ResponseCode.OK);
	}

	public static HttpResponse xsltResponse(String xml, String xslFileName, ResponseCode code) throws IOException {
		StringReader reader = null;
		StringWriter writer = null;
		try {
			reader = new StringReader(xml);
			writer = new StringWriter();
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer(
					new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream(xslFileName)));
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
		if (content == null) {
			throw new IllegalArgumentException();
		}
		byte[] bytes = content.getBytes(MAIN_ENCODING);
		return new HttpResponse(code, contentType, bytes, null, bytes.length);
	}

	public static HttpResponse textResponse(Object content, ResponseCode code, String contentType) {

		return textResponse(content.toString(), code, contentType);
	}

	public static HttpResponse noContent() {
		return textResponse("", ResponseCode.NO_CONTENT, ContentType.PLAIN);
	}

	public static HttpResponse ok() {
		return textResponse("Ok", ResponseCode.OK, ContentType.PLAIN);
	}

	public static HttpResponse exceptionResponse(Exception e, ResponseCode code) {
		return exceptionResponse(e, code, false);
	}

	public static HttpResponse exceptionResponse(Exception e, ResponseCode code, boolean debugMode) {
		String text;
		if (!debugMode) {
			text = e.getClass().getName();
		} else {
			text = TextUtil.getStringFromException(e);
		}
		return textResponse(text, code, ContentType.PLAIN);
	}

	public static HttpResponse exceptionResponse(Exception e, boolean debugMode) {
		return exceptionResponse(e, ResponseCode.INTERNAL_SERVER_ERROR, debugMode);
	}

	public static HttpResponse exceptionResponse(Exception e) {
		return exceptionResponse(e, false);
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
		if (content != null)
			return content.clone();
		return new byte[0];
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

	public static class MapEntryConverter implements Converter {

		@Override
		public boolean canConvert(Class clazz) {
			return AbstractMap.class.isAssignableFrom(clazz);
		}

		public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {

			AbstractMap<?, ?> map = (AbstractMap<?, ?>) value;
			for (Object obj : map.entrySet()) {
				Map.Entry<?, ?> entry = (Map.Entry<?, ?>) obj;
				writer.startNode(entry.getKey().toString());
				Object val = entry.getValue();
				if (null != val) {
					writer.setValue(val.toString());
				}
				writer.endNode();
			}

		}

		public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

			Map<String, String> map = new HashMap<>();

			while (reader.hasMoreChildren()) {
				reader.moveDown();

				String key = reader.getNodeName(); // nodeName aka element's
													// name
				String value = reader.getValue();
				map.put(key, value);

				reader.moveUp();
			}

			return map;
		}

	}

}
