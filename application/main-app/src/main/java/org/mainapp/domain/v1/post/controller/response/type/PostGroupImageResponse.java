package org.mainapp.domain.v1.post.controller.response.type;

import org.domainmodule.postgroup.entity.PostGroupImage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "게시물 그룹 이미지 응답 객체")
public class PostGroupImageResponse {

	@Schema(description = "게시물 그룹 이미지 id", example = "1")
	private Long id;

	@Schema(description = "게시물 그룹 id", example = "1")
	private Long postGroupId;

	@Schema(description = "이미지 URL", example = "https://~")
	private String url;

	public static PostGroupImageResponse from(PostGroupImage postGroupImage) {
		return new PostGroupImageResponse(
			postGroupImage.getId(),
			postGroupImage.getPostGroup().getId(),
			postGroupImage.getUrl()
		);
	}
}
