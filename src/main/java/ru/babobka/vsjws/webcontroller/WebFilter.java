package ru.babobka.vsjws.webcontroller;

import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;

public interface WebFilter {

	public HttpResponse onFilter(HttpRequest request);

	public void afterFilter(HttpRequest request, HttpResponse response);

}
