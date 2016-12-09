package ru.babobka.vsjws.model;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class HttpResponseTest {

	@Test
	public void testOkResponse() {
		HttpResponse response = HttpResponse.ok();
		assertEquals(response.getResponseCode(), HttpResponse.ResponseCode.OK);
		assertNotEquals(response.getContent().length, 0);
	}

	@Test
	public void testExceptionResponse() {
		HttpResponse response = HttpResponse.exceptionResponse(new NullPointerException());
		assertNotEquals(response.getResponseCode(), HttpResponse.ResponseCode.OK);
		assertNotEquals(response.getContent().length, 0);
	}

	@Test
	public void testEmptyResponse() {
		HttpResponse response = HttpResponse.noContent();
		assertEquals(response.getResponseCode(), HttpResponse.ResponseCode.NO_CONTENT);
		assertEquals(response.getContent().length, 0);
	}

	@Test
	public void testFileResponse() throws IOException {
		File currentJavaFile = new File("pom.xml");
		HttpResponse response = HttpResponse.fileResponse(currentJavaFile);
		assertEquals(response.getResponseCode(), HttpResponse.ResponseCode.OK);
		assertEquals(response.getContent().length,0);
		assertNotNull(response.getFile());
	}

	@Test
	public void testResourceResponse() throws IOException {

		HttpResponse response = HttpResponse.resourceResponse("test.txt");
		assertEquals(response.getResponseCode(), HttpResponse.ResponseCode.OK);
		try {
			HttpResponse.resourceResponse("test1.txt");
			fail();
		} catch (FileNotFoundException e) {

		}

	}

	@Test
	public void testSimpleJsonResponse() {
		try {
			HttpResponse.jsonResponse("fail");
			fail();
		} catch (JSONException e) {

		}
		HttpResponse response = HttpResponse.jsonResponse(new JSONObject());
		assertEquals(response.getResponseCode(), HttpResponse.ResponseCode.OK);
		assertTrue(response.getContent().length > 0);
		assertNull(response.getFile());

	}

	@Test
	public void testJsonMapResponse() {
		String key = "abc";
		Map<String, String> testJsonMap = new HashMap<>();
		testJsonMap.put(key, "xyz");
		HttpResponse response = HttpResponse.jsonResponse(testJsonMap);
		JSONObject responseJSON = new JSONObject(new String(response.getContent()));
		JSONObject expectedJSON = new JSONObject(testJsonMap);
		assertEquals(responseJSON.toString(), expectedJSON.toString());
		assertFalse(responseJSON.isNull(key));
	}

	@Test
	public void testJsonObjectResponse() {
		Object objectToJson = new Object();
		HttpResponse response = HttpResponse.jsonResponse(objectToJson);
		JSONObject responseJSON = new JSONObject(new String(response.getContent()));
		JSONObject expectedJSON = new JSONObject(objectToJson);
		assertEquals(responseJSON.toString(), expectedJSON.toString());
	}

	@Test
	public void testJsonNullResponse() {
		
		try {
			HttpResponse.jsonResponse(null);
			fail();
		} catch (IllegalArgumentException e) {

		}
	}

	@Test
	public void testJsonListResponse() {
		List<String> list = new LinkedList<>();
		list.add("abc");
		list.add("xyz");
		list.add("qwe");
		HttpResponse response = HttpResponse.jsonResponse(list);
		JSONArray responseJSON = new JSONArray(new String(response.getContent()));
		assertEquals(responseJSON.length(), list.size());

	}

	@Test
	public void testJsonArrayResponse() {
		int[] array = { 1, 2, 3 };
		HttpResponse response = HttpResponse.jsonResponse(array);
		assertTrue(response.getContent().length > 0);
		JSONArray responseJSON = new JSONArray(new String(response.getContent()));
		JSONArray expectedJSON = new JSONArray(array);
		assertEquals(responseJSON.toString(), expectedJSON.toString());
		assertEquals(responseJSON.length(), array.length);
	}

}
