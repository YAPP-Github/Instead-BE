package org.mainapp.domain.v1.user.exception;

import org.mainapp.global.error.ErrorCodeStatus;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCodeStatus {
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User를 찾을 수 없습니다.");
	private final HttpStatus httpStatus;
	private final String message;
}
