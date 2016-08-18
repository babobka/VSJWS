package ru.babobka.vsjws.webcontroller;

import java.io.File;
import java.io.IOException;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;

public class StaticResourcesController extends WebController {

	private final String webContentFolder;

	public StaticResourcesController(String webContentFolder) {
		this.webContentFolder = webContentFolder;

	}

	@Override
	public HttpResponse onGet(HttpRequest request) throws IOException {
		String uri = request.getUri();
		String fileName = uri.replace('/', File.separatorChar).replace(
				"web-content", "");
		File file = new File(webContentFolder + fileName);
		if (file.exists() && file.isFile()) {
			return HttpResponse.fileResponse(file);
		} else {
			return HttpResponse.NOT_FOUND_RESPONSE;
		}
	}

	@Override
	public HttpResponse onHead(HttpRequest request) throws IOException {
		return onGet(request);
	}

}
