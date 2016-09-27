package ru.babobka.vsjws.webserver;

import ru.babobka.vsjsw.logger.SimpleLogger;
import ru.babobka.vsjws.constant.RegularExpressions;
import ru.babobka.vsjws.listener.OnExceptionListener;
import ru.babobka.vsjws.listener.OnServerStartListener;
import ru.babobka.vsjws.model.HttpSession;
import ru.babobka.vsjws.runnable.SocketProcessorRunnable;
import ru.babobka.vsjws.util.TextUtil;
import ru.babobka.vsjws.webcontroller.WebController;

import java.io.File;
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
public class WebServer {

	public final Map<String, WebController> controllerHashMap = new ConcurrentHashMap<>();

	private final String name;

	private volatile OnServerStartListener onServerStartListener;

	private volatile OnExceptionListener onExceptionListener;

	private volatile ServerSocket ss;

	private final String webContentFolder;

	private static final int DEFAULT_SESSION_TIME_OUT_SEC = 900;

	private static final int SOCKET_READ_TIMEOUT_MILLIS = 2000;

	private static final int MAX_PORT = 65536;

	private static final int THREAD_POOL_SIZE = 10;

	private static final int BACKLOG = 10;

	private volatile boolean running = false;

	private final HttpSession httpSession;

	private final Integer sessionTimeOutSeconds;

	private final String logFolder;

	private final SimpleLogger logger;

	private volatile ExecutorService threadPool;

	private final int port;

	private volatile Thread startThread;

	public WebServer(String name, int port, String webContentFolder, String logFolder) throws IOException {
		this(name, port, DEFAULT_SESSION_TIME_OUT_SEC, webContentFolder, logFolder);
	}

	public WebServer(String name, int port, Integer sessionTimeOutSeconds, String webContentFolder, String logFolder)
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
		if (webContentFolder != null) {
			File folder = new File(webContentFolder);
			if (folder.isDirectory()) {
				if (!folder.exists()) {
					folder.mkdirs();
				}
			} else {
				throw new IllegalArgumentException("'webContentFolder' is invalid");
			}
		}
		logger = new SimpleLogger(name + ":" + port, logFolder, name);
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

		logger.log(Level.INFO, "Web server name:\t" + getBeautifulName());
		logger.log(Level.INFO, "Web server log folder:\t" + logFolder);
		logger.log(Level.INFO, "Web server content folder:\t" + webContentFolder);
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

	private void runBlocking() throws IOException {

		try {

			logger.log(Level.INFO, "Running server " + getBeautifulName());
			threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
			OnServerStartListener listener = onServerStartListener;
			if (listener != null) {
				listener.onStart(name, port);
			}
			ServerSocket ss = this.ss;
			while (running) {
				try {
					Socket s = ss.accept();
					s.setSoTimeout(SOCKET_READ_TIMEOUT_MILLIS);
					threadPool.execute(new SocketProcessorRunnable(s, controllerHashMap, httpSession, logger,
							webContentFolder, onExceptionListener));
				} catch (IOException e) {
					if (running && !ss.isClosed()) {
						e.printStackTrace();
					} else {
						threadPool.shutdown();
						break;
					}
				}

			}
		} finally {
			ExecutorService threadPool = this.threadPool;
			if (threadPool != null) {
				threadPool.shutdown();
			}
			ServerSocket ss = this.ss;
			if (ss != null) {
				ss.close();
			}

		}
		logger.log(Level.INFO, "Server " + getBeautifulName() + " is done");
	}

	public void run() throws IOException {

		if (!running) {
			synchronized (this) {
				if (!running) {

					if (startThread != null) {
						try {
							logger.log(Level.INFO, "Wait starting thread to join");
							startThread.join();
							logger.log(Level.INFO, "Done waitig starting thread");
						} catch (InterruptedException e) {
							e.printStackTrace();
							throw new IOException(e);
						}
					}
					ss = new ServerSocket(port, BACKLOG);
					running = true;
					startThread = new Thread(new Runnable() {

						@Override
						public void run() {

							try {
								WebServer.this.runBlocking();
							} catch (Exception e) {
								logger.log(Level.SEVERE, e);
							}

						}
					});
					startThread.start();

				} else {
					logger.log(Level.WARNING, "Can not run already running server " + getBeautifulName());

				}
			}
		} else {
			logger.log(Level.WARNING, "Can not run already running server " + getBeautifulName());
		}

	}

	public void stop() {
		logger.log(Level.INFO, "Try to stop server " + getBeautifulName());
		if (running) {
			synchronized (this) {
				if (running) {
					running = false;
					try {
						ServerSocket ss = this.ss;
						if (ss != null && !ss.isClosed()) {
							ss.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			logger.log(Level.WARNING, "Can't stop server " + getBeautifulName() + ". It wasn't running.");

		}
		logger.log(Level.INFO, "Done stopping " + getBeautifulName());

	}

	public int getPort() {
		return port;
	}

	public String getBeautifulName() {
		return TextUtil.beautifyServerName(name, port);
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

	public void setOnServerStartListener(OnServerStartListener onServerStartListener) {
		this.onServerStartListener = onServerStartListener;
	}

	public static void main(String[] args) {
		System.out.println("VSJWS");
	}

	public boolean isRunning() {
		return running;
	}

}
