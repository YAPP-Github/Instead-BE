package org.uploadscheduleapplication.util.dto;

import org.domainmodule.post.entity.Post;
import org.domainmodule.snstoken.entity.SnsToken;

public record UploadPostDto(
	Post post,
	SnsToken snsToken
) {
	public static UploadPostDto fromPost(Post post) {
		return new UploadPostDto(
			post,
			post.getPostGroup().getAgent().getUser().getSnsToken()
		);
	}
}

