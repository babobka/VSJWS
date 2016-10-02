package ru.babobka.vsjws.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import ru.babobka.vsjws.util.HttpUtil;
import ru.babobka.vsjws.util.TextUtil;

public class RawHttpRequest {

	private final Map<String, String> headers = new HashMap<>();

	private String body;

	private String firstLine;

	public RawHttpRequest(String firstLine, Map<String, String> headers, String body) {
		super();
		this.body = body;
		this.firstLine = firstLine;
		if (headers != null)
			this.headers.putAll(headers); 
	}

	public RawHttpRequest(InputStream is) throws IOException {
		int row = 0;
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = br.readLine()) != null) {
			if (line.isEmpty()) {
				int contentLength = TextUtil.tryParseInt(headers.get(HttpRequest.CONTENT_LENGTH_HEADER));
				this.body = HttpUtil.readBody(contentLength, br);
				break;
			}
			if (row == 0) {
				firstLine = line;
			} else {
				Header header = new Header(line);
				headers.put(header.getKey(), header.getValue());
			}
			row++;
		}

	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getBody() {
		return body;
	}

	public String getFirstLine() {
		return firstLine;
	}

	@Override
	public String toString() {
		return "RawHttpRequest [headers=" + headers + ", body=" + body + ", firstLine=" + firstLine + "]";
	}
	
	

}
