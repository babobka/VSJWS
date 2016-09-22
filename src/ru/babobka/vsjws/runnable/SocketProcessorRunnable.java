package ru.babobka.vsjws.runnable;

import ru.babobka.vsjsw.logger.SimpleLogger;
import ru.babobka.vsjws.constant.Method;
import ru.babobka.vsjws.exception.BadProtocolSpecifiedException;
import ru.babobka.vsjws.exception.InvalidContentLengthException;
import ru.babobka.vsjws.listener.OnExceptionListener;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.model.HttpResponse.ResponseCode;
import ru.babobka.vsjws.model.HttpSession;
import ru.babobka.vsjws.util.HttpUtil;
import ru.babobka.vsjws.webcontroller.StaticResourcesController;
import ru.babobka.vsjws.webcontroller.WebController;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.logging.Level;

/**
 * Created by dolgopolov.a on 25.12.15.
 */
public class SocketProcessorRunnable implements Runnable {

	private final Socket s;
	private final HttpSession httpSession;
	private final Map<String, WebController> controllerMap;
	private final SimpleLogger logger;
	private final StaticResourcesController staticResourcesController;
	private final OnExceptionListener onExceptionListener;
	private final String webContentFolder;

	public SocketProcessorRunnable(Socket s, Map<String, WebController> controllerMap, HttpSession httpSession,
			SimpleLogger logger, String webContentFolder, OnExceptionListener onExceptionListener) throws IOException {
		this.s = s;
		this.httpSession = httpSession;
		this.controllerMap = controllerMap;
		this.onExceptionListener = onExceptionListener;
		this.logger = logger;
		this.staticResourcesController = new StaticResourcesController(webContentFolder);
		this.webContentFolder = webContentFolder;
	}

	@Override
	public void run() {
		HttpResponse response = HttpResponse.NOT_FOUND_RESPONSE;
		boolean noContent = false;
		try {
			HttpRequest request = new HttpRequest(s.getInetAddress(), s.getInputStream(), httpSession);
			if (request.getMethod().equals(Method.HEAD)) {
				noContent = true;
			}
			if (request.getUri() != null) {
				String sessionId = request.getCookies().get(HttpRequest.SESSION_ID);
				if (sessionId == null) {
					sessionId = HttpUtil.generateSessionId();
					response.addCookie(HttpRequest.SESSION_ID, sessionId);
				}
				if (!httpSession.exists(sessionId)) {
					httpSession.create(sessionId);
				}
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

			}
		} catch (BadProtocolSpecifiedException e) {
			response = HttpResponse.exceptionResponse(e, ResponseCode.HTTP_VERSION_NOT_SUPPORTED);
		} catch (InvalidContentLengthException e) {
			response = HttpResponse.exceptionResponse(e, ResponseCode.LENGTH_REQUIRED);
		} catch (IllegalArgumentException e) {
			response = HttpResponse.exceptionResponse(e, ResponseCode.BAD_REQUEST);
		} catch (SocketTimeoutException e) {
			response = HttpResponse.exceptionResponse(e, ResponseCode.REQUEST_TIMEOUT);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e);
			if (onExceptionListener != null) {
				try {
					response = onExceptionListener.onException(e);
				} catch (Exception e1) {
					logger.log(Level.SEVERE, e1);
					response = HttpResponse.exceptionResponse(e1);
				}
			} else {
				response = HttpResponse.exceptionResponse(e);
			}
		} finally {
			try {
				HttpUtil.writeResponse(s.getOutputStream(), response, noContent);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if (s != null)
					s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
