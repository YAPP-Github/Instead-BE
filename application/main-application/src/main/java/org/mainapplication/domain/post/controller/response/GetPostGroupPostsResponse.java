package org.mainapplication.domain.post.controller.response;

import java.util.List;

import org.domainmodule.post.entity.Post;
import org.domainmodule.postgroup.entity.PostGroup;
import org.mainapplication.domain.post.controller.response.type.PostGroupResponse;
import org.mainapplication.domain.post.controller.response.type.PostResponse;

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
	private List<PostResponse> posts;

	public static GetPostGroupPostsResponse of(PostGroup postGroup, List<Post> posts) {
		List<PostResponse> postResponses = posts.stream()
			.map(PostResponse::from)
			.toList();
		return new GetPostGroupPostsResponse(PostGroupResponse.from(postGroup), postResponses);
	}
}
