package ru.babobka.vsjws.webcontroller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;

public class StaticResourcesController extends WebController {

	@Override
	public HttpResponse onGet(HttpRequest request) throws IOException {
		String uri = request.getUri();
		String fileName = uri.replace('/', File.separatorChar).replaceFirst(File.separator, "");
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
		if (is != null) {
			return HttpResponse.resourceResponse(is);
		} else {
			return HttpResponse.NOT_FOUND_RESPONSE;
		}

	}

}
