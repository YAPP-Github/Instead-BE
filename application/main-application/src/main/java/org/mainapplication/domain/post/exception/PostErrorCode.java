package org.mainapplication.domain.post.exception;

import org.mainapplication.global.error.ErrorCodeStatus;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements ErrorCodeStatus {

	NO_NEWS_CATEGORY(HttpStatus.BAD_REQUEST, "뉴스와 함께 생성하려면, 뉴스 카테고리를 함께 선택해주세요."),
	NO_IMAGE_URLS(HttpStatus.BAD_REQUEST, "이미지와 함께 생성하려면, 이미지를 첨부해주세요."),
	NEWS_FEED_EXHAUSTED(HttpStatus.BAD_REQUEST, "현재 최신 피드를 전부 사용했습니다. 피드가 갱신될 때까지 시간이 걸릴 수 있습니다."),
	POST_GENERATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류로 게시물 생성에 실패했습니다. 관리자에게 문의하세요."),
	NEWS_GET_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류로 뉴스 가져오기에 실패했습니다. 관리자에게 문의하세요."),

	PROMPT_HISTORIES_NOT_FOUND(HttpStatus.NOT_FOUND, "프롬프트 내역을 찾을 수 없습니다.");
	private final HttpStatus httpStatus;
	private final String message;
}
