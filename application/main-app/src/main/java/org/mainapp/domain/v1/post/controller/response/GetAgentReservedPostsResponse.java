package org.mainapp.domain.v1.post.controller.response;

import java.util.List;

import org.domainmodule.post.entity.Post;
import org.mainapp.domain.v1.post.controller.response.type.PostResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "계정별 예약 게시물 목록 조회 API 응답 객체")
public record GetAgentReservedPostsResponse(
	@Schema(description = "예약 게시물 목록")
	List<PostResponse> posts
) {

	public static GetAgentReservedPostsResponse from(List<Post> posts) {
		List<PostResponse> postResponses = posts.stream()
			.map(PostResponse::from)
			.toList();
		return new GetAgentReservedPostsResponse(postResponses);
	}
}
