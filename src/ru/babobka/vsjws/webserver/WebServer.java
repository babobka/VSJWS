package ru.babobka.vsjws.webserver;

import ru.babobka.nodeLogger.NodeLogger;
import ru.babobka.vsjws.constant.RegularExpressions;
import ru.babobka.vsjws.listener.OnExceptionListener;
import ru.babobka.vsjws.listener.OnServerStartListener;
import ru.babobka.vsjws.model.HttpSession;
import ru.babobka.vsjws.runnable.SocketProcessorRunnable;
import ru.babobka.vsjws.webcontroller.WebController;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * Created by dolgopolov.a on 30.12.15.
 */
public class WebServer {

	public final Map<String, WebController> controllerHashMap = new HashMap<>();

	private final String name;

	private volatile OnServerStartListener onServerStartListener;

	private volatile OnExceptionListener onExceptionListener;

	private volatile ServerSocket ss;

	private final String webContentFolder;

	private static final int DEFAULT_SESSION_TIME_OUT_SEC = 900;

	private static final int SOCKET_READ_TIMEOUT_MILLIS = 1000;

	private static final int BACKLOG = 10;

	private volatile boolean stopped;

	private volatile boolean running;

	private final HttpSession httpSession;

	private final Integer sessionTimeOutSeconds;

	private final String logFolder;

	private final NodeLogger logger;

	private volatile ExecutorService threadPool;

	private final int port;

	public WebServer(String name, int port, String webContentFolder,
			String logFolder) throws IOException {
		this(name, port, DEFAULT_SESSION_TIME_OUT_SEC, webContentFolder,
				logFolder);
	}

	public WebServer(String name, int port, Integer sessionTimeOutSeconds,
			String webContentFolder, String logFolder) throws IOException {
		if (port < 0 || port > Short.MAX_VALUE) {
			throw new IllegalArgumentException(
					"Port must be in range [0;65536)");
		}
		if (sessionTimeOutSeconds != null && sessionTimeOutSeconds < 0) {
			throw new IllegalArgumentException("Session time out must be > 0");
		}
		if (name == null) {
			throw new IllegalArgumentException("Web server name is null");
		} else if (!name.matches(RegularExpressions.FILE_NAME_PATTERN)) {
			throw new IllegalArgumentException(
					"Web server name must contain letters,numbers and spaces only");
		}
		if (logFolder == null) {
			throw new IllegalArgumentException("Log folder is null");
		}
		if (webContentFolder != null) {
			File folder = new File(webContentFolder);
			if (!folder.exists()) {
				throw new IllegalArgumentException(
						"Web content folder doesn't exist");
			}
		}
		logger = new NodeLogger(WebServer.class, logFolder, name);
		this.name = name;
		this.webContentFolder = webContentFolder;
		this.sessionTimeOutSeconds = sessionTimeOutSeconds;
		this.logFolder = logFolder;
		this.port = port;
		if (sessionTimeOutSeconds == null) {
			this.httpSession = new HttpSession(DEFAULT_SESSION_TIME_OUT_SEC);
		} else {
			this.httpSession = new HttpSession(sessionTimeOutSeconds);
		}

		logger.log(Level.INFO, "Web server name:\t" + name);
		logger.log(Level.INFO, "Web server port:\t" + port);
		logger.log(Level.INFO, "Web server log folder:\t" + logFolder);
		logger.log(Level.INFO, "Web server content folder:\t"
				+ webContentFolder);
	}

	public OnServerStartListener getOnServerStartListener() {
		return onServerStartListener;
	}

	public void addController(String uri, WebController webController) {
		if (uri.startsWith("/")) {
			controllerHashMap.put(uri, webController);
		} else {
			controllerHashMap.put("/" + uri, webController);
		}
	}

	public String getWebContentFolder() {
		return webContentFolder;
	}

	public int getSessionTimeOutSeconds() {
		return sessionTimeOutSeconds;
	}

	public String getLogFolder() {
		return logFolder;
	}

	public void run() throws IOException {

		if (!running) {
			synchronized (this) {
				if (!running) {
					stopped = false;
					running = true;
				} else {
					return;
				}
			}

			try {
				this.ss = new ServerSocket(port, BACKLOG);
				threadPool = Executors.newFixedThreadPool(10);
				OnServerStartListener listener = onServerStartListener;
				if (listener != null) {
					listener.onStart(name, port);
				}
				while (!stopped) {
					try {
						Socket s = ss.accept();
						s.setSoTimeout(SOCKET_READ_TIMEOUT_MILLIS);
						threadPool.execute(new SocketProcessorRunnable(s,
								controllerHashMap, httpSession, logger,
								webContentFolder, onExceptionListener));
					} catch (IOException e) {
						if (!stopped) {
							e.printStackTrace();
						} else {
							threadPool.shutdown();
							break;
						}
					}

				}
			} finally {
				running = false;
				stopped = true;
				if (threadPool != null) {
					threadPool.shutdown();
				}
				if (this.ss != null) {
					this.ss.close();
				}
			}

		}

	}

	public void stop() {
		stopped = true;
		running = false;
		try {
			if (ss != null) {
				ss.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getPort() {
		return port;
	}

	public HttpSession getHttpSession() {
		return httpSession;
	}

	public String getName() {
		return name;
	}

	public OnExceptionListener getOnExceptionListener() {
		return onExceptionListener;
	}

	public void setOnExceptionListener(OnExceptionListener onExceptionListener) {
		this.onExceptionListener = onExceptionListener;
	}

	public void setOnServerStartListener(
			OnServerStartListener onServerStartListener) {
		this.onServerStartListener = onServerStartListener;
	}

	public static void main(String[] args) {
		System.out.println("VSJWS");
	}

}
