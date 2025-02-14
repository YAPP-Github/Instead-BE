package org.mainapp.domain.post.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "게시물 프롬프트 기반 개별 수정 API 요청 본문")
public record SinglePostUpdateRequest(
	@Schema(description = "수정에 사용할 프롬프트", example = "이렇게 이렇게 수정해줘")
	@NotNull(message = "수정에 사용할 프롬프트를 입력해주세요.")
	String prompt
) {
	public static SinglePostUpdateRequest from(String prompt) {
		return new SinglePostUpdateRequest(prompt);
	}
}
