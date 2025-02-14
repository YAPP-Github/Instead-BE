package org.mainapp.domain.sns.exception;

import org.mainapp.global.error.ErrorCodeStatus;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SnsErrorCode implements ErrorCodeStatus {

	TWITTER_USER_INFO_FETCH_FAILED(HttpStatus.BAD_REQUEST, "Twitter 유저 기본정보를 가져오지 못했습니다.");
	private final HttpStatus httpStatus;
	private final String message;
}
