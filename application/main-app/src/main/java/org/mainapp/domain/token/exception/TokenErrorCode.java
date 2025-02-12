package org.mainapp.domain.token.exception;

import org.mainapp.global.error.ErrorCodeStatus;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenErrorCode implements ErrorCodeStatus {

	REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "RefreshToken이 존재하지 않습니다."),
	REFRESH_TOKEN_NOT_MATCHED(HttpStatus.CONFLICT, "RefreshToken이 일치하지 않습니다."),
	REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "RefreshToken이 만료되었습니다."),
	ACCESS_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "AccessToken이 존재하지 않습니다."),
	ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AccessToken이 만료되었습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
