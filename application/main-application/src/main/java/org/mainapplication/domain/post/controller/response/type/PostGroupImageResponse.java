package org.mainapplication.domain.post.controller.response.type;

import org.domainmodule.postgroup.entity.PostGroupImage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema
public class PostGroupImageResponse {

	private Long id;

	private Long postGroupId;

	private String url;

	public static PostGroupImageResponse from(PostGroupImage postGroupImage) {
		return new PostGroupImageResponse(
			postGroupImage.getId(),
			postGroupImage.getPostGroup().getId(),
			postGroupImage.getUrl()
		);
	}
}
