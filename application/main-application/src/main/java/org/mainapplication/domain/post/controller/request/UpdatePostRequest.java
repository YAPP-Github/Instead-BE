package org.mainapplication.domain.post.controller.request;

public record UpdatePostRequest(
	String prompt
) {
	public static UpdatePostRequest from(String prompt) {
		return new UpdatePostRequest(prompt);
	}
}
