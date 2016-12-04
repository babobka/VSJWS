package ru.babobka.vsjws.webserver;

import ru.babobka.vsjws.constant.RegularExpressions;
import ru.babobka.vsjws.listener.OnExceptionListener;
import ru.babobka.vsjws.listener.OnServerStartListener;
import ru.babobka.vsjws.logger.SimpleLogger;

import ru.babobka.vsjws.model.HttpSession;
import ru.babobka.vsjws.runnable.SocketProcessorRunnable;
import ru.babobka.vsjws.runnable.WebController;
import ru.babobka.vsjws.util.TextUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * Created by dolgopolov.a on 30.12.15.
 */
public class WebServer extends Thread {

	private final Map<String, WebController> controllerMap = new ConcurrentHashMap<>();

	private final Map<String, OnExceptionListener> exceptionListenerMap = new ConcurrentHashMap<>();

	private final String name;

	private volatile OnServerStartListener onServerStartListener;

	private volatile ServerSocket ss;

	private static final int DEFAULT_SESSION_TIME_OUT_SEC = 900;

	private static final int SOCKET_READ_TIMEOUT_MILLIS = 2000;

	private volatile boolean debugMode;

	private static final int MAX_PORT = 65536;

	private static final int THREAD_POOL_SIZE = 10;

	private static final int BACKLOG = 25;

	private final HttpSession httpSession;

	private final Integer sessionTimeOutSeconds;

	private final String logFolder;

	private final SimpleLogger logger;

	private volatile ExecutorService threadPool;

	private final int port;

	public WebServer(String name, int port, String logFolder) throws IOException {
		this(name, port, DEFAULT_SESSION_TIME_OUT_SEC, logFolder, false);
	}

	public WebServer(String name, int port, Integer sessionTimeOutSeconds, String logFolder, boolean debugMode)
			throws IOException {
		if (port < 0 || port > MAX_PORT) {
			throw new IllegalArgumentException("Port must be in range [0;" + MAX_PORT + ")");
		}
		if (sessionTimeOutSeconds != null && sessionTimeOutSeconds < 0) {
			throw new IllegalArgumentException("Session time out must be > 0");
		}
		if (name == null) {
			throw new IllegalArgumentException("Web server name is null");
		} else if (!name.matches(RegularExpressions.FILE_NAME_PATTERN)) {
			throw new IllegalArgumentException("Web server name must contain letters,numbers and spaces only");
		}
		if (logFolder == null) {
			throw new IllegalArgumentException("Log folder is null");
		}

		logger = new SimpleLogger(name + ":" + port, logFolder, name);
		this.name = name;

		this.sessionTimeOutSeconds = sessionTimeOutSeconds;
		this.logFolder = logFolder;
		this.port = port;
		this.debugMode = debugMode;
		if (sessionTimeOutSeconds == null) {
			this.httpSession = new HttpSession(DEFAULT_SESSION_TIME_OUT_SEC);
		} else {
			this.httpSession = new HttpSession(sessionTimeOutSeconds);
		}

		logger.log("Web server name:\t" + getFullName());
		logger.log("Web server log folder:\t" + logFolder);
		if (debugMode) {
			logger.log(Level.WARNING, "Debug mode is on");
		}
		this.ss = new ServerSocket(port, BACKLOG);
	}

	public OnServerStartListener getOnServerStartListener() {
		return onServerStartListener;
	}

	public void addController(String uri, WebController webController) {
		if (uri.startsWith("/")) {
			controllerMap.put(uri, webController);
		} else {
			controllerMap.put("/" + uri, webController);
		}
	}

	public int getSessionTimeOutSeconds() {
		return sessionTimeOutSeconds;
	}

	public String getLogFolder() {
		return logFolder;
	}

	public void run() {
		ServerSocket localServerSocket = null;
		try {
			logger.log("Run server " + getFullName());
			threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
			OnServerStartListener listener = onServerStartListener;
			if (listener != null) {
				listener.onStart(name, port);
			}
			localServerSocket = ss;
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Socket s = localServerSocket.accept();
					s.setSoTimeout(SOCKET_READ_TIMEOUT_MILLIS);
					threadPool.execute(new SocketProcessorRunnable(s, controllerMap, httpSession, logger,
							exceptionListenerMap, debugMode));
				} catch (IOException e) {
					if (!localServerSocket.isClosed()) {
						logger.log(e);
					} else {
						threadPool.shutdown();
						break;
					}
				}

			}
		} finally {
			clear();

		}
		logger.log("Server " + getFullName() + " is done");
	}

	private void clear() {
		ExecutorService localThreadPool = this.threadPool;
		if (localThreadPool != null) {
			localThreadPool.shutdown();
		}
		try {
			ServerSocket localServerSocket = this.ss;
			if (localServerSocket != null && !localServerSocket.isClosed()) {
				localServerSocket.close();
			}
		} catch (IOException e) {
			logger.log(e);
		}
		controllerMap.clear();
		exceptionListenerMap.clear();
	}

	@Override
	public void interrupt() {
		super.interrupt();
		clear();

	}

	public int getPort() {
		return port;
	}

	public String getFullName() {
		return TextUtil.beautifyServerName(name, port);
	}

	public HttpSession getHttpSession() {
		return httpSession;
	}

	public SimpleLogger getLogger() {
		return logger;
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	public Map<String, OnExceptionListener> addExceptionListener(Class<? extends Exception> exceptionClass,
			OnExceptionListener onExceptionListener) {
		this.exceptionListenerMap.put(exceptionClass.getName(), onExceptionListener);
		return exceptionListenerMap;
	}

	public void setOnServerStartListener(OnServerStartListener onServerStartListener) {
		this.onServerStartListener = onServerStartListener;
	}

}
