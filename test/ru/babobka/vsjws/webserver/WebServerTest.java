package ru.babobka.vsjws.webserver;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WebServerTest {

	private static final int PORT = 2512;

	private static final int SESSION_TIMEOUT_SECS = 15 * 60;

	private static final String SERVER_NAME = "Sample server";

	private static final String LOG_FOLDER = "server_log";

	private int tests = 10;

	private WebServer webServer;

	@Before
	public void init() throws IOException {
		webServer = new WebServer(SERVER_NAME, PORT, SESSION_TIMEOUT_SECS, null, LOG_FOLDER);
	}

	@After
	public void stopServer() {
		if (webServer != null) {
			webServer.stop();
		}

	}

	@Test
	public void testRun() throws IOException {
		for (int i = 0; i < tests; i++) {
			webServer.run();
			assertTrue(webServer.isRunning());
		}
	}

	@Test
	public void testStop() throws IOException {
		for (int i = 0; i < tests; i++) {
			webServer.stop();
			assertTrue(webServer.isStopped());
		}
	}

	@Test
	public void testRunStopDelay() throws IOException, InterruptedException {
		for (int i = 0; i < tests; i++) {
			webServer.run();
			assertTrue(webServer.isRunning());
			Thread.sleep(200);
			webServer.stop();
			assertTrue(webServer.isStopped());
		}
	}

	@Test
	public void testRunStop() throws IOException, InterruptedException {
		for (int i = 0; i < tests; i++) {
			webServer.run();
			assertTrue(webServer.isRunning());
			webServer.stop();
			assertTrue(webServer.isStopped());
		}
	}
}
