package org.mainapplication.domain.post.controller.response.type;

import java.time.LocalDateTime;
import java.util.List;

import org.domainmodule.post.entity.Post;
import org.domainmodule.post.entity.type.PostStatusType;
import org.springframework.lang.Nullable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostResponse {

	private Long id;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private String summary;

	private String content;

	private List<PostImageResponse> postImages;

	private PostStatusType status;

	@Nullable
	private LocalDateTime uploadTime;

	public static PostResponse from(Post post) {
		return new PostResponse(
			post.getId(),
			post.getCreatedAt(),
			post.getUpdatedAt(),
			post.getSummary(),
			post.getContent(),
			null,
			post.getStatus(),
			post.getUploadTime()
		);
	}
}
