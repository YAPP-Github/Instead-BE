package org.mainapplication.domain.post.controller.response.type;

import org.domainmodule.post.entity.PostImage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "게시물 이미지 응답 객체")
public class PostImageResponse {

	@Schema(description = "게시물 이미지 id", example = "1")
	private Long id;

	@Schema(description = "게시물 id", example = "1")
	private Long postId;

	@Schema(description = "이미지 URL", example = "https://~")
	private String url;

	public static PostImageResponse from(PostImage postImage) {
		return new PostImageResponse(
			postImage.getId(),
			postImage.getPost().getId(),
			postImage.getUrl()
		);
	}
}
