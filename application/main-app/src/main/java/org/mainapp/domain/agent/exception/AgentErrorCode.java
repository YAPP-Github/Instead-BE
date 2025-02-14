package org.mainapp.domain.agent.exception;

import org.mainapp.global.error.ErrorCodeStatus;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgentErrorCode implements ErrorCodeStatus {

	AGENT_NOT_FOUND(HttpStatus.NOT_FOUND, "계정을 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
