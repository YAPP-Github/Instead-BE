package org.mainapp.domain.v1.post.controller.request;

import org.domainmodule.postgroup.entity.type.PostGroupStepType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(description = "게시물 그룹 단계 수정 API 요청 본문")
public record UpdatePostGroupStepRequest(
	@Schema(description = "게시물 예약일시 수정 정보 리스트")
	@NotNull(message = "변경할 게시물 그룹 단계를 입력해주세요.")
	@Valid
	PostGroupStepType step
) {
}
