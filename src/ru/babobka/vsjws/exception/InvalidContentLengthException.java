package ru.babobka.vsjws.exception;

public class InvalidContentLengthException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1106415477334743940L;

	public InvalidContentLengthException(String message) {
		super(message);
	}

	public InvalidContentLengthException() {
		super();
	}

}
