package ru.babobka.vsjws.model;

import java.net.InetAddress;

import org.json.JSONObject;

public class JSONHttpRequest extends HttpRequest {

	private JSONObject json = new JSONObject();

	public JSONHttpRequest(InetAddress address, RawHttpRequest rawHttpRequest, HttpSession httpSession) {
		super(address, rawHttpRequest, httpSession);
		if (this.getContentLength() > 0) {
			this.json = new JSONObject(this.getBody());
		}
	}

	public JSONObject getJSONBody() {
		return json;
	}

}
