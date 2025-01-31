package org.mainapplication.global.error;

import org.springframework.http.HttpStatus;

public interface ErrorCodeStatus {
	String name();

	HttpStatus getHttpStatus();

	String getMessage();
}
