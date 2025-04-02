package org.mainapp.domain.v1.post.exception;

import org.mainapp.global.error.ErrorCodeStatus;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements ErrorCodeStatus {

	// TODO: repository 계층 NotFound 에러코드는 특정 패키지 하위에서 관리하지 않도록 리팩토링하기
	POST_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "게시물 그룹이 존재하지 않습니다."),
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시물이 존재하지 않습니다."),
	RSS_CURSOR_NOT_FOUND(HttpStatus.NOT_FOUND, "게시물 그룹에 RSS cursor가 존재하지 않습니다."),
	RSS_FEED_NOT_FOUND(HttpStatus.NOT_FOUND, "RSS 피드가 존재하지 않습니다."),
	PROMPT_HISTORIES_NOT_FOUND(HttpStatus.NOT_FOUND, "프롬프트 내역을 찾을 수 없습니다."),

	// BAD_REQUEST
	NO_NEWS_CATEGORY(HttpStatus.BAD_REQUEST, "뉴스와 함께 생성하려면, 뉴스 카테고리를 함께 선택해주세요."),
	NO_IMAGE_URLS(HttpStatus.BAD_REQUEST, "이미지와 함께 생성하려면, 이미지를 첨부해주세요."),
	INVALID_POST(HttpStatus.BAD_REQUEST, "게시물 그룹에 해당하는 게시물이 아닙니다."),
	INVALID_DELETING_POST_STATUS(HttpStatus.BAD_REQUEST, "업로드가 확정된 게시물은 삭제할 수 없습니다."),
	EXHAUSTED_GENERATION_COUNT(HttpStatus.BAD_REQUEST, "게시물 생성 가능 횟수를 전부 사용했습니다. 새로운 게시물 그룹을 이용해주세요."),
	EXHAUSTED_NEWS_FEED(HttpStatus.BAD_REQUEST, "현재 최신 피드를 전부 사용했습니다. 피드가 갱신될 때까지 시간이 걸릴 수 있습니다."),
	INVALID_UPDATING_POST_TYPE(HttpStatus.BAD_REQUEST, "수정하려는 필드를 updateType에 맞게 정확히 포함해주세요."),

	// INTERNAL_SERVER_ERROR
	POST_GENERATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류로 게시물 생성에 실패했습니다. 관리자에게 문의하세요."),
	NEWS_GET_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류로 뉴스 가져오기에 실패했습니다. 관리자에게 문의하세요.");

	private final HttpStatus httpStatus;
	private final String message;
}
