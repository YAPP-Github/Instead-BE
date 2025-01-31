package org.uploadscheduleapplication.util.dto;

import org.domainmodule.post.entity.Post;
import org.domainmodule.snstoken.entity.SnsToken;

public record UploadPostDto(
	Post post,
	SnsToken snsToken
) {
	public static UploadPostDto fromPost(Post post) {
		SnsToken snsToken = post.getPostGroup().getAgent().getSnsToken();
		return new UploadPostDto(
			post,
			snsToken
		);
	}

	public static UploadPostDto from(Post post, SnsToken snsToken) {
		return new UploadPostDto(
			post,
			snsToken
		);
	}
}

