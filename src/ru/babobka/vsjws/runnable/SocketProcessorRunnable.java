package ru.babobka.vsjws.runnable;

import ru.babobka.nodeLogger.NodeLogger;
import ru.babobka.vsjws.constant.Method;
import ru.babobka.vsjws.constant.ContentType;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.model.HttpSession;
import ru.babobka.vsjws.util.HttpUtil;
import ru.babobka.vsjws.util.TextUtil;
import ru.babobka.vsjws.webcontroller.StaticResourcesController;
import ru.babobka.vsjws.webcontroller.WebController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;

/**
 * Created by dolgopolov.a on 25.12.15.
 */
public class SocketProcessorRunnable implements Runnable {

	private final Socket s;
	private final InputStream is;
	private final OutputStream os;
	private final HttpSession httpSession;
	private final Map<String, WebController> controllerMap;
	private final NodeLogger logger;
	private final StaticResourcesController staticResourcesController;
	private final String webContentFolder;

	public SocketProcessorRunnable(Socket s,
			Map<String, WebController> controllerMap,
			HttpSession httpSession, NodeLogger logger, String webContentFolder)
			throws IOException {
		this.s = s;
		this.is = s.getInputStream();
		this.os = s.getOutputStream();
		this.httpSession = httpSession;
		this.controllerMap = controllerMap;
		this.logger = logger;
		this.staticResourcesController = new StaticResourcesController(
				webContentFolder);
		this.webContentFolder = webContentFolder;
	}

	@Override
	public void run() {
		HttpResponse response = HttpResponse.NOT_FOUND_RESPONSE;
		boolean noContent = false;
		try {
			HttpRequest request = new HttpRequest(s.getInetAddress(),is, httpSession);
			if (request.getMethod() == null) {
				return;
			}
			if (request.getMethod().equals(Method.HEAD)) {
				noContent = true;
			}
			if (request.getUri() != null) {
				WebController webController;
				if (request.getUri().startsWith("/web-content")) {
					if (webContentFolder != null) {
						webController = staticResourcesController;
						response = webController.onGet(request);
					}
				} else if (controllerMap.containsKey(request.getUri())) {
					webController = controllerMap.get(request.getUri());
					response = webController.control(request);
				}
				String sessionId = request.getCookies().get(
						HttpRequest.SESSION_ID);
				if (sessionId == null) {
					sessionId = HttpUtil.generateSessionId();
					response.addCookie(HttpRequest.SESSION_ID, sessionId);
					httpSession.create(sessionId);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			String stackTrace = TextUtil.getStringFromException(e);
			logger.log(Level.SEVERE, stackTrace);
			response = HttpResponse.textResponse(stackTrace,
					HttpResponse.ResponseCode.INTERNAL_SERVER_ERROR,
					ContentType.PLAIN);
		} finally {
			try {
				HttpUtil.writeResponse(os, response, noContent);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if (s != null)
					s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (os != null)
					os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
