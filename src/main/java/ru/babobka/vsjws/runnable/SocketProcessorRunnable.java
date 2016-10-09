package ru.babobka.vsjws.runnable;

import ru.babobka.vsjws.constant.Method;
import ru.babobka.vsjws.exception.BadProtocolSpecifiedException;
import ru.babobka.vsjws.exception.InvalidContentLengthException;
import ru.babobka.vsjws.listener.OnExceptionListener;
import ru.babobka.vsjws.logger.SimpleLogger;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.model.HttpResponse.ResponseCode;
import ru.babobka.vsjws.model.HttpSession;
import ru.babobka.vsjws.model.RawHttpRequest;
import ru.babobka.vsjws.util.HttpUtil;
import ru.babobka.vsjws.webcontroller.StaticResourcesController;
import ru.babobka.vsjws.webcontroller.WebController;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Created by dolgopolov.a on 25.12.15.
 */
public class SocketProcessorRunnable implements Runnable {

	private final Socket s;
	private final HttpSession httpSession;
	private final Map<String, WebController> controllerMap;
	private final Map<String, OnExceptionListener> exceptionListenerMap;
	private final SimpleLogger logger;
	private final StaticResourcesController staticResourcesController;

	public SocketProcessorRunnable(Socket s, Map<String, WebController> controllerMap, HttpSession httpSession,
			SimpleLogger logger, Map<String, OnExceptionListener> exceptionListenerMap) throws IOException {
		this.s = s;
		this.httpSession = httpSession;
		this.controllerMap = controllerMap;
		this.exceptionListenerMap = exceptionListenerMap;
		this.logger = logger;
		this.staticResourcesController = new StaticResourcesController();

	}

	@Override
	public void run() {
		HttpResponse response = HttpResponse.NOT_FOUND_RESPONSE;
		boolean noContent = false;
		try {
			HttpRequest request = new HttpRequest(s.getInetAddress(), new RawHttpRequest(s.getInputStream()),
					httpSession);
			if (request.getMethod().equals(Method.HEAD)) {
				noContent = true;
			}
			String cleanedUri = HttpUtil.cleanUri(request.getUri());
			if (cleanedUri != null) {
				String sessionId = request.getCookies().get(HttpRequest.SESSION_ID_HEADER);
				if (sessionId == null) {
					sessionId = HttpUtil.generateSessionId();
					response.addCookie(HttpRequest.SESSION_ID_HEADER, sessionId);
				}
				if (!httpSession.exists(sessionId)) {
					httpSession.create(sessionId);
				}
				WebController webController;
				if (cleanedUri.startsWith("/web-content")) {
					webController = staticResourcesController;
					response = webController.onGet(request);
				} else if ((webController = controllerMap.get(cleanedUri)) != null) {
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
			OnExceptionListener onExceptionListener = exceptionListenerMap.get(e.getClass().getName());
			if (onExceptionListener != null) {
				try {
					response = onExceptionListener.onException(e);
					if (response == null) {
						response = HttpResponse.exceptionResponse(e);
					}
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
				logger.log(Level.SEVERE, e1);
			}
			try {
				s.close();
			} catch (IOException e) {
				logger.log(Level.SEVERE, e);
			}

		}
	}

}
