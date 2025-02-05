package org.mainapplication.domain.post.controller.response.type;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "게시물 이미지 응답 객체")
public class PostImageResponse {

	@Schema(description = "게시물 이미지 id", example = "1")
	private Long id;

	@Schema(description = "게시물 id", example = "1")
	private Long postId;

	@Schema(description = "이미지 URL", example = "https://~")
	private String url;
}
