package ru.babobka.vsjws.model;

import ru.babobka.vsjws.constant.Method;
import ru.babobka.vsjws.exception.BadProtocolSpecifiedException;
import ru.babobka.vsjws.exception.InvalidContentLengthException;
import ru.babobka.vsjws.util.HttpUtil;
import ru.babobka.vsjws.util.TextUtil;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dolgopolov.a on 30.12.15.
 */
public class HttpRequest {

	public static final String CONTENT_LENGTH_HEADER = "Content-Length";

	public static final String HOST_HEADER = "Host";

	public static final String SESSION_ID_HEADER = "X-Session-Id";

	public static final String PROTOCOL = "HTTP/1.1";

	private String method;

	private String uri;

	private String body;

	private int contentLength = -1;

	private final Map<String, String> params = new HashMap<>();

	private final Map<String, String> urlParams = new HashMap<>();

	private final Map<String, String> cookies = new HashMap<>();

	private final Map<String, String> headers = new HashMap<>();

	private final HttpSession httpSession;

	private final InetAddress address;

	public HttpRequest(InetAddress address, RawHttpRequest rawHttpRequest, HttpSession httpSession) {
		this.headers.putAll(rawHttpRequest.getHeaders());
		if (!headers.containsKey(HOST_HEADER)) {
			throw new IllegalArgumentException("Header 'Host' was not set");
		}
		if (rawHttpRequest.getFirstLine() == null) {
			throw new IllegalArgumentException("First line is empty");
		}
		String[] firstLineArray = rawHttpRequest.getFirstLine().split(" ");
		if (firstLineArray.length < 3) {
			throw new IllegalArgumentException("Bad first line");
		}
		this.method = firstLineArray[0];
		this.contentLength = TextUtil.tryParseInt(headers.get(HttpRequest.CONTENT_LENGTH_HEADER), -1);
		if (method == null) {
			throw new IllegalArgumentException("HTTP method was not specified");
		} else if (!Method.isValidMethod(method)) {
			throw new IllegalArgumentException("HTTP method is invalid");
		} else if (isMethodWithContent(method) && contentLength == -1) {
			throw new InvalidContentLengthException("'Content-Length' header wasn't set properly");
		}
		this.uri = firstLineArray[1];
		if (!firstLineArray[2].equals(PROTOCOL)) {
			throw new BadProtocolSpecifiedException();
		}
		this.cookies.putAll(HttpUtil.getCookies(rawHttpRequest.getHeaders().getOrDefault("Cookie", "")));
		this.body = rawHttpRequest.getBody();
		this.params.putAll(HttpUtil.getParams(body));
		this.urlParams.putAll(HttpUtil.getUriParams(uri));
		this.address = address;
		this.httpSession = httpSession;

	}

	public String getParam(String key) {
		return params.getOrDefault(key, "");
	}

	private boolean isMethodWithContent(String method) {
		return (method.equals(Method.PATCH) || method.equals(Method.POST) || method.equals(Method.PUT));

	}

	public Map<String, Serializable> getSession() {
		return httpSession.get(cookies.get(SESSION_ID_HEADER));

	}

	public Map<String, String> getCookies() {
		return cookies;
	}

	public String getUrlParam(String key) {
		return urlParams.getOrDefault(key, "");
	}

	public String getHeader(String key) {
		return headers.getOrDefault(key, "");

	}

	public Map<String, String> getUrlParams() {
		return urlParams;
	}

	public String getUri() {
		return uri;
	}

	public String getMethod() {
		return method;
	}

	public String getBody() {
		return body;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		result = prime * result + ((urlParams == null) ? 0 : urlParams.hashCode());
		return result;
	}

	public InetAddress getAddress() {
		return address;
	}

	public int getContentLength() {
		return contentLength;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HttpRequest other = (HttpRequest) obj;
		if (body == null) {
			if (other.body != null)
				return false;
		} else if (!body.equals(other.body))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		if (urlParams == null) {
			if (other.urlParams != null)
				return false;
		} else if (!urlParams.equals(other.urlParams))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "HttpRequest [method=" + method + ", uri=" + uri + ", body=" + body + ", contentLength=" + contentLength
				+ ", params=" + params + ", urlParams=" + urlParams + ", cookies=" + cookies + ", headers=" + headers
				+ ", httpSession=" + httpSession + ", address=" + address + "]";
	}

	
	
}
