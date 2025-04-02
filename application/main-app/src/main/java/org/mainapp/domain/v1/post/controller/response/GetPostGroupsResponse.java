package org.mainapp.domain.v1.post.controller.response;

import java.util.List;

import org.domainmodule.postgroup.entity.PostGroup;
import org.mainapp.domain.v1.post.controller.response.type.PostGroupResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "계정별 게시물 그룹 목록 조회 API 응답 본문")
public record GetPostGroupsResponse(
	@Schema(description = "게시물 그룹 목록")
	List<PostGroupResponse> postGroups
) {

	public static GetPostGroupsResponse from(List<PostGroup> postGroups) {
		List<PostGroupResponse> postGroupResponses = postGroups.stream()
			.map(PostGroupResponse::from)
			.toList();
		return new GetPostGroupsResponse(
			postGroupResponses
		);
	}
}
