package com.hwtxframework.ioc.exceptions;

public class MiniContainerException extends RuntimeException {

	private static final long serialVersionUID = 2937078211721288748L;

	public MiniContainerException() {
		super();
	}

	public MiniContainerException(String message) {
		super(message);
	}

	public MiniContainerException(String message, Throwable cause) {
		super(message, cause);
	}

	public MiniContainerException(Throwable cause) {
		super(cause);
	}

}
