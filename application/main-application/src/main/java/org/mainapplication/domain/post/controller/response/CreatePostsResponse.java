package org.mainapplication.domain.post.controller.response;

import java.util.List;

import org.mainapplication.domain.post.controller.response.type.PostResponse;

import lombok.Getter;

@Getter
public class CreatePostsResponse {

	private Long postGroupId;

	private Boolean eof;

	private List<PostResponse> posts;
}
