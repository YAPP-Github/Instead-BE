package org.mainapp.domain.v1.auth.exception;

import org.mainapp.global.error.ErrorCodeStatus;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCodeStatus {
	// 인증 에러
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "세션이 만료되었습니다."),
	AUTH_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "인증 정보를 찾을 수 없습니다.");
	private final HttpStatus httpStatus;
	private final String message;
}
