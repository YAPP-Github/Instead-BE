package org.mainapplication.global.error.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCodeStatus {
	String name();

	HttpStatus getHttpStatus();

	String getMessage();
}
