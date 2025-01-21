package org.mainapplication.domain.post.controller.response.type;

import java.time.LocalDateTime;
import java.util.List;

import org.domainmodule.post.entity.type.PostStatus;
import org.springframework.lang.Nullable;

import lombok.Getter;

@Getter
public class PostResponse {

	private Long id;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private String summary;

	private String content;

	private List<PostImageResponse> postImages;

	private PostStatus status;

	@Nullable
	private LocalDateTime uploadTime;
}
