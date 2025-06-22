package org.mainapp.domain.v1.post.controller.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "게시물 프롬프트 기반 일괄 수정 API 요청 본문")
public record MultiplePostUpdateRequest(
	@Schema(description = "일괄 수정에 사용할 프롬프트", example = "말투를 부드럽게 전부 수정해줘")
	@NotNull(message = "수정에 사용할 프롬프트를 입력해주세요.")
	String prompt,
	@Schema(description = "일괄 수정 할 Post의 ID값 리스트")
	@NotNull(message = "수정할 게시물 목록을 입력해주세요.")
	List<Long> postsId
) {
}
