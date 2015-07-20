package com.hwtxframework.ioc.exceptions;

public class UnResolveException extends RuntimeException {

	public UnResolveException() {
		super();
	}

	public UnResolveException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnResolveException(String message) {
		super(message);
	}

	public UnResolveException(Throwable cause) {
		super(cause);
	}

}
