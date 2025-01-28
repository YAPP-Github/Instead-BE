package org.mainapplication.global.error;

import java.io.Serializable;

import lombok.Getter;

@Getter
public abstract class CustomException extends RuntimeException implements Serializable {

	private final transient ErrorCodeStatus errorCodeStatus;

	protected CustomException(ErrorCodeStatus errorCodeStatus) {
		super(errorCodeStatus.getMessage());
		this.errorCodeStatus = errorCodeStatus;
	}
}