package ru.babobka.vsjws.model;

import net.jodah.expiringmap.ExpiringMap;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by dolgopolov.a on 12.01.16.
 */
public class HttpSession {

	private final Map<String, ConcurrentHashMap<String, Serializable>> expiringMap;

	public HttpSession(int seconds) {
		expiringMap = ExpiringMap.builder()
				.expirationPolicy(ExpiringMap.ExpirationPolicy.CREATED)
				.expiration(seconds, TimeUnit.SECONDS).build();

	}

	public Map<String, Serializable> get(String sessionId) {
		Map<String, Serializable> map;

		if (!expiringMap.containsKey(sessionId)) {
			synchronized (this) {
				if (expiringMap.containsKey(sessionId)) {
					expiringMap.put(sessionId,
							new ConcurrentHashMap<String, Serializable>());
				}
			}
		}
		map = expiringMap.get(sessionId);
		return map;
	}

	public boolean exists(String sessionId) {
		return expiringMap.containsKey(sessionId);
	}

	public void create(String sessionId) {
		expiringMap.put(sessionId,
				new ConcurrentHashMap<String, Serializable>());

	}

}
