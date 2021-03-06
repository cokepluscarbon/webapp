package com.demo.webapp.service.exception;

public class PasswordIncorrectException extends Exception {
	private static final long serialVersionUID = 1L;

	public PasswordIncorrectException() {
		super("密码错误异常");
	}

	public PasswordIncorrectException(String message) {
		super(message);
	}

	public PasswordIncorrectException(String message, Throwable cause) {
		super(message, cause);
	}

	public PasswordIncorrectException(Throwable cause) {
		super(cause);
	}

}
