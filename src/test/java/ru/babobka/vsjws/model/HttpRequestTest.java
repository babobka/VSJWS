package ru.babobka.vsjws.model;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import ru.babobka.vsjws.exception.BadProtocolSpecifiedException;
import ru.babobka.vsjws.exception.InvalidContentLengthException;

public class HttpRequestTest {

	private static RawHttpRequest emptyRequest;

	private static RawHttpRequest invalidFirstLineRequest;

	private static RawHttpRequest badHttpProtocolRequest;

	private static RawHttpRequest noHeadersRequest;

	private static RawHttpRequest badMethodRequest;

	private static Map<String, String> normalHeaders;

	private static Map<String, String> badContentLengthHeaders;

	private static RawHttpRequest badContentLengthRequest;

	private static RawHttpRequest normalRequest;

	@BeforeClass
	public static void init() {
		normalHeaders = new HashMap<>();
		normalHeaders.put("Host", "test");
		badContentLengthHeaders = new HashMap<>();
		badContentLengthHeaders.putAll(normalHeaders);
		badContentLengthHeaders.put("Content-Length", "-1");
		emptyRequest = new RawHttpRequest(null, null, null);
		invalidFirstLineRequest = new RawHttpRequest("abc", null, null);
		badHttpProtocolRequest = new RawHttpRequest("GET / HTTP/2", normalHeaders, null);
		noHeadersRequest = new RawHttpRequest("GET / HTTP/1.1", null, null);
		normalRequest = new RawHttpRequest(noHeadersRequest.getFirstLine(), normalHeaders, null);
		badMethodRequest = new RawHttpRequest("GETS / HTTP/1.1", null, null);
		badContentLengthRequest = new RawHttpRequest(noHeadersRequest.getFirstLine(), badContentLengthHeaders, null);
	}

	@Test
	public void testEmptyRequest() {
		try {
			new HttpRequest(null, emptyRequest, null);
			fail();
		} catch (IllegalArgumentException e) {

		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testInvalidFirstLineRequest() {
		try {
			new HttpRequest(null, invalidFirstLineRequest, null);
			fail();
		} catch (IllegalArgumentException e) {

		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testBadProtocolRequest() {
		try {
			new HttpRequest(null, badHttpProtocolRequest, null);
			fail();
		} catch (BadProtocolSpecifiedException e) {
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testNoHeadersRequest() {
		try {
			new HttpRequest(null, noHeadersRequest, null);
			fail();
		} catch (IllegalArgumentException e) {

		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testBadMethodRequest() {
		try {
			new HttpRequest(null, badMethodRequest, null);
		} catch (IllegalArgumentException e) {

		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testBadContentLengthRequest() {
		try {
			new HttpRequest(null, badContentLengthRequest, null);
		} catch (InvalidContentLengthException e) {

		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testNormalRequest() {
		new HttpRequest(null, normalRequest, null);
	}

}
