package org.mainapplication.domain.agent.exception;

import org.mainapplication.global.error.ErrorCodeStatus;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgentErrorCode implements ErrorCodeStatus {

	AGENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Agent를 찾을 수 없습니다.");
	private final HttpStatus httpStatus;
	private final String message;
}
