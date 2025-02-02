package org.mainapplication.domain.post.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시물 프롬프트 기반 개별 수정 API 요청 본문")
public record UpdatePostRequest(
	@Schema(description = "수정에 사용할 프롬프트", example = "이렇게 이렇게 수정해줘")
	String prompt
) {
	public static UpdatePostRequest from(String prompt) {
		return new UpdatePostRequest(prompt);
	}
}
