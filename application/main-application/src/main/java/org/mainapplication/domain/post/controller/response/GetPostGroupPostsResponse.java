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
@Schema
public class GetPostGroupPostsResponse {

	private PostGroupResponse postGroup;

	private List<PostResponse> posts;

	public static GetPostGroupPostsResponse of(PostGroup postGroup, List<Post> posts) {
		List<PostResponse> postResponses = posts.stream()
			.map(PostResponse::from)
			.toList();
		return new GetPostGroupPostsResponse(PostGroupResponse.from(postGroup), postResponses);
	}
}
