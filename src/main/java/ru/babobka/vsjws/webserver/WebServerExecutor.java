package ru.babobka.vsjws.webserver;

import java.util.logging.Level;

public class WebServerExecutor {

	private final WebServer server;

	private volatile Thread startThread;

	private volatile boolean running;

	private volatile boolean starting;

	public WebServerExecutor(WebServer server) {
		this.server = server;
	}

	public void stop() {
		while (starting) {
			Thread.yield();
		}
		server.getLogger().log("Try to stop server " + server.getFullName());
		if (running) {
			synchronized (this) {
				if (running) {
					running = false;
					server.stop();
				}
			}
		} else {
			server.getLogger().log(Level.WARNING, "Can't stop server " + server.getFullName() + ". It wasn't running.");

		}
		server.getLogger().log("Done stopping " + server.getFullName());
	}

	public void run() {

		if (!running && !starting) {
			synchronized (this) {
				if (!running && !starting) {
					starting = true;
					if (startThread != null) {
						try {
							server.getLogger().log("Wait starting thread to join");
							if (startThread.isAlive())
								startThread.join();
							server.getLogger().log("Done waitig starting thread");
						} catch (InterruptedException e) {
							startThread.interrupt();

						}
					}

					startThread = new Thread(new Runnable() {

						@Override
						public void run() {
							running = true;
							starting = false;
							try {
								server.run();
							} catch (Exception e) {
								server.getLogger().log(e);
							} finally {
								running = false;
							}

						}
					});
					startThread.start();
					while (starting) {
						Thread.yield();
					}

				}
			}
		} else {
			server.getLogger().log(Level.WARNING, "Can not run already running server " + server.getFullName());
		}

	}

	public boolean isRunning() {
		while (starting) {
			Thread.yield();
		}
		return running;
	}

}
