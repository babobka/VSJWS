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

	private static final String LOG_FOLDER = "/Users/bbk/Documents/nodes/log";

	private int tests = 15;

	private WebServerExecutor serverExecutor;

	@Before
	public void init() throws IOException {
		serverExecutor = new WebServerExecutor(new WebServer(SERVER_NAME, PORT, SESSION_TIMEOUT_SECS, LOG_FOLDER));
	}

	@After
	public void stopServer() {

		serverExecutor.stop();

	}

	@Test
	public void testRun() throws IOException {
		for (int i = 0; i < tests; i++) {
			serverExecutor.run();
			assertTrue(serverExecutor.isRunning());
		}
	}

	@Test
	public void testStop() throws IOException {
		for (int i = 0; i < tests; i++) {
			serverExecutor.stop();
			assertFalse(serverExecutor.isRunning());
		}
	}

	@Test
	public void testRunStopDelay() throws IOException, InterruptedException {
		for (int i = 0; i < tests; i++) {
			serverExecutor.run();
			assertTrue(serverExecutor.isRunning());
			Thread.sleep(200);
			serverExecutor.stop();
			assertFalse(serverExecutor.isRunning());
		}
	}

	@Test
	public void testRunStop() throws IOException, InterruptedException {
		for (int i = 0; i < tests; i++) {
			serverExecutor.run();
			assertTrue(serverExecutor.isRunning());
			serverExecutor.stop();
			assertFalse(serverExecutor.isRunning());
		}
	}
}
