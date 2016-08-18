package ru.babobka.vsjws.model;

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

	private final String method;

	private final String host;

	private final String uri;

	private final String content;

	private final HashMap<String, String> params = new HashMap<>();

	private final HashMap<String, String> urlParams = new HashMap<>();

	private final HashMap<String, String> cookies = new HashMap<>();

	private final HashMap<String, String> headers = new HashMap<>();

	private final HttpSession httpSession;

	private final InetAddress address;

	public HttpRequest(InetAddress address, InputStream is,
			HttpSession httpSession) throws IOException {
		this.address = address;
		String method = null, uri = null, host = null;
		int row = 0, contentLength = 0;
		String uriParamsString = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		while (true) {
			String line = br.readLine();
			if (line == null || line.trim().length() == 0) {
				this.content = HttpUtil.getContent(contentLength, br);
				break;
			}
			if (row == 0) {
				String[] startingLine = line.split(" ");
				method = startingLine[0];
				uri = startingLine[1];
				String[] uriArray = uri.split("\\?");
				uri = uriArray[0];
				if (uriArray.length > 1) {
					uriParamsString = uriArray[1];
				}

			} else if (row == 1) {
				host = line.substring(line.indexOf(':') + 2, line.length());
			} else if (line.startsWith("Cookie:")) {
				cookies.putAll(HttpUtil.getCookies(line));
			} else {
				String[] header = line.split(":");
				headers.put(header[0], header[1].substring(1));
			}
			if (contentLength == 0 && line.startsWith(CONTENT_LENGTH)) {
				contentLength = Integer.parseInt(line.substring(
						line.indexOf(':') + 2, line.length()));
			}
			row++;
		}

		this.method = method;
		this.host = host;
		this.uri = uri;
		params.putAll(HttpUtil.getParams(content));
		urlParams.putAll(HttpUtil.getParams(uriParamsString));
		this.httpSession = httpSession;
	}

	public String getParam(String key) {
		return params.get(key);
	}

	public Map<String, Serializable> getSession() {
		String sessionId = cookies.get(SESSION_ID);
		return httpSession.get(sessionId);

	}

	public HashMap<String, String> getCookies() {
		return cookies;
	}

	public String getUrlParam(String key) {
		return urlParams.get(key);
	}

	public String getHeader(String key) {
		return headers.get(key);
	}

	public HashMap<String, String> getUrlParams() {
		return urlParams;
	}

	@Override
	public String toString() {
		return "HttpRequest{" + "params=" + params + ", method='" + method
				+ '\'' + ", host='" + host + '\'' + ", uri='" + uri + '\''
				+ ", content='" + content + '\'' + ", cookies=" + cookies + '}';
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
		result = prime * result
				+ ((urlParams == null) ? 0 : urlParams.hashCode());
		return result;
	}

	
	
	public InetAddress getAddress() {
		return address;
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
