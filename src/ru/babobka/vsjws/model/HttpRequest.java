package ru.babobka.vsjws.model;

import ru.babobka.vsjws.constant.Method;
import ru.babobka.vsjws.exception.BadProtocolSpecifiedException;
import ru.babobka.vsjws.exception.InvalidContentLengthException;
import ru.babobka.vsjws.util.HttpUtil;

import java.io.*;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dolgopolov.a on 30.12.15.
 */
public class HttpRequest {

	private static final String CONTENT_LENGTH = "Content-Length";

	public static final String SESSION_ID = "X-Session-Id";

	public static final String PROTOCOL = "HTTP/1.1";

	private String method;

	private String host;

	private String uri;

	private String content;

	private int contentLength = -1;

	private final Map<String, String> params = new HashMap<>();

	private final Map<String, String> urlParams = new HashMap<>();

	private final Map<String, String> cookies = new HashMap<>();

	private final Map<String, String> headers = new HashMap<>();

	private final HttpSession httpSession;

	private final InetAddress address;

	public HttpRequest(InetAddress address, InputStream is, HttpSession httpSession) throws IOException {
		this.address = address;
		int row = 0, contentLength = 0;
		String uriParamsString = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = br.readLine()) != null) {
			if (line.isEmpty()) {
				if (method != null && isMethodWithContent(method)) {
					this.content = HttpUtil.getContent(contentLength, br);
				}
				break;
			}
			if (row == 0) {
				String[] startingLine = line.split(" ");
				if (startingLine.length < 3) {
					throw new IllegalArgumentException("Bad first line");
				} else {
					method = startingLine[0];
					uri = startingLine[1];
					if (!startingLine[2].equals(PROTOCOL)) {
						throw new BadProtocolSpecifiedException();
					}
					String[] uriArray = uri.split("\\?");
					uri = uriArray[0];

					if (uriArray.length > 1) {
						uriParamsString = uriArray[1];
					}
				}

			} else if (row == 1) {
				host = line.substring(line.indexOf(':') + 2, line.length());
			} else if (line.startsWith("Cookie:")) {
				cookies.putAll(HttpUtil.getCookies(line));
			} else if (contentLength == 0 && line.startsWith(CONTENT_LENGTH)) {
				contentLength = Integer.parseInt(line.substring(line.indexOf(':') + 2, line.length()));
				if (contentLength < 0) {
					throw new InvalidContentLengthException("'Content-Length' header wasn't set properly");
				}
				this.contentLength = contentLength;
			} else {
				String[] header = line.split(":");
				if (header.length >= 2) {
					headers.put(header[0], header[1].substring(1));
				}
			}
			row++;
		}
		if (method == null) {
			throw new IllegalArgumentException("HTTP method was not specified");
		} else if (isMethodWithContent(method) && contentLength == -1) {
			throw new InvalidContentLengthException("'Content-Length' header wasn't set properly");
		}
		this.params.putAll(HttpUtil.getParams(content));
		this.urlParams.putAll(HttpUtil.getParams(uriParamsString));
		this.httpSession = httpSession;
	}

	public String getParam(String key) {
		String param = params.get(key);
		if (param != null) {
			return param;
		}
		return "";
	}

	private boolean isMethodWithContent(String method) {
		return (method.equals(Method.PATCH) || method.equals(Method.POST) || method.equals(Method.PUT));

	}

	public Map<String, Serializable> getSession() {
		String sessionId = cookies.get(SESSION_ID);
		System.out.println(sessionId);
		return httpSession.get(sessionId);

	}

	public Map<String, String> getCookies() {
		return cookies;
	}

	public String getUrlParam(String key) {
		String param = urlParams.get(key);
		if (param != null) {
			return param;
		}
		return "";
	}

	public String getHeader(String key) {
		String header = headers.get(key);
		if (header != null) {
			return header;
		}
		return "";
	}

	public Map<String, String> getUrlParams() {
		return urlParams;
	}

	@Override
	public String toString() {
		return "HttpRequest [method=" + method + ", host=" + host + ", uri=" + uri + ", content=" + content
				+ ", params=" + params + ", urlParams=" + urlParams + ", cookies=" + cookies + ", headers=" + headers
				+ ", httpSession=" + httpSession + ", address=" + address + "]";
	}

	public String getContent() {
		return content;
	}

	public String getUri() {
		return uri;
	}

	public String getHost() {
		return host;
	}

	public String getMethod() {
		return method;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
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
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
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

}
