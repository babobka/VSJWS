package ru.babobka.vsjws.listener;

import ru.babobka.vsjws.model.HttpResponse;

public interface OnExceptionListener {

	public HttpResponse onException(Exception e);

}
