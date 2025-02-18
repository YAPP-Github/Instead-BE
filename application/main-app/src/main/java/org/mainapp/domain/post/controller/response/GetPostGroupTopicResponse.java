package org.mainapp.domain.post.controller.response;

import org.domainmodule.postgroup.entity.PostGroup;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시물 그룹 주제 조회 API 응답 본문")
public record GetPostGroupTopicResponse(
	@Schema(description = "게시물 그룹 주제", example = "햄부기 먹기")
	String topic
) {

	public static GetPostGroupTopicResponse from(PostGroup postGroup) {
		return new GetPostGroupTopicResponse(postGroup.getTopic());
	}
}
