package ru.babobka.vsjws.webcontroller;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import ru.babobka.vsjws.constant.Method;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;

/**
 * Created by dolgopolov.a on 30.12.15.
 */
public class WebController {

	private final List<WebFilter> webFilters = new LinkedList<>();

	public WebController addWebFilter(WebFilter webFilter) {
		webFilters.add(webFilter);
		return this;
	}

	public final HttpResponse control(HttpRequest request) throws Exception {

		ListIterator<WebFilter> li = webFilters.listIterator(webFilters.size());
		HttpResponse response = null;
		while (li.hasPrevious()) {
			response = li.previous().onFilter(request);
			if (response != null) {
				break;
			}
		}
		if (response == null) {
			switch (request.getMethod()) {
			case Method.GET:
				response = onGet(request);
				break;
			case Method.POST:
				response = onPost(request);
				break;
			case Method.DELETE:
				response = onDelete(request);
				break;
			case Method.PUT:
				response = onPut(request);
				break;
			case Method.PATCH:
				response = onPatch(request);
				break;
			case Method.HEAD:
				response = onHead(request);
				break;
			default:
				response = HttpResponse.NOT_IMPLEMENTED_RESPONSE;
				break;
			}
		}
		li = webFilters.listIterator(webFilters.size());
		while (li.hasPrevious()) {
			li.previous().afterFilter(request, response);

		}

		return response;
	}

	public HttpResponse onHead(HttpRequest request) throws Exception {
		return HttpResponse.NOT_IMPLEMENTED_RESPONSE;
	}

	public HttpResponse onGet(HttpRequest request) throws Exception {
		return HttpResponse.NOT_IMPLEMENTED_RESPONSE;
	}

	public HttpResponse onPost(HttpRequest request) throws Exception {
		return HttpResponse.NOT_IMPLEMENTED_RESPONSE;
	}

	public HttpResponse onPut(HttpRequest request) throws Exception {
		return HttpResponse.NOT_IMPLEMENTED_RESPONSE;
	}

	public HttpResponse onPatch(HttpRequest request) throws Exception {
		return HttpResponse.NOT_IMPLEMENTED_RESPONSE;
	}

	public HttpResponse onDelete(HttpRequest request) throws Exception {
		return HttpResponse.NOT_IMPLEMENTED_RESPONSE;
	}
}
