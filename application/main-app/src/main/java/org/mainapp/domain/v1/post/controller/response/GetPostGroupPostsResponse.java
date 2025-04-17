package org.mainapp.domain.v1.post.controller.response;

import java.util.List;
import java.util.Map;

import org.domainmodule.post.entity.type.PostStatusType;
import org.domainmodule.postgroup.entity.PostGroup;
import org.mainapp.domain.v1.post.controller.response.type.PostGroupResponse;
import org.mainapp.domain.v1.post.controller.response.type.PostResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "게시물 그룹별 게시물 목록 조회 API 응답 본문")
public class GetPostGroupPostsResponse {

	@Schema(description = "게시물 그룹 정보")
	private PostGroupResponse postGroup;

	@Schema(description = "게시물 그룹에 해당하는 게시물 목록")
	private Map<PostStatusType, List<PostResponse>> posts;

	public static GetPostGroupPostsResponse of(PostGroup postGroup, Map<PostStatusType, List<PostResponse>> posts) {
		return new GetPostGroupPostsResponse(PostGroupResponse.from(postGroup), posts);
	}
}
