package com.hwtxframework.ioc.exceptions;

public class LoadConfigException extends RuntimeException {

	private static final long serialVersionUID = -4519691994232529065L;

	public LoadConfigException() {
		super();

	}

	public LoadConfigException(String message) {
		super(message);

	}

	public LoadConfigException(String message, Throwable cause) {
		super(message, cause);

	}

	public LoadConfigException(Throwable cause) {
		super(cause);

	}

}
