package org.mainapp.domain.v1.post.controller.response;

import org.domainmodule.postgroup.entity.PostGroup;
import org.domainmodule.postgroup.entity.type.PostGroupStepType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시물 그룹 단계 조회 API 응답 본문")
public record GetPostGroupStepResponse(
	@Schema(description = "게시물 그룹 단계", example = "EDITING")
	PostGroupStepType step
) {
	
	public static GetPostGroupStepResponse from(PostGroup postGroup) {
		return new GetPostGroupStepResponse(postGroup.getStep());
	}
}
