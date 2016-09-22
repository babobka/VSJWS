package ru.babobka.vsjws.exception;

public class BadProtocolSpecifiedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7902624905314878886L;

	public BadProtocolSpecifiedException(String message) {
		super(message);
	}

	public BadProtocolSpecifiedException() {
		super();
	}

}
