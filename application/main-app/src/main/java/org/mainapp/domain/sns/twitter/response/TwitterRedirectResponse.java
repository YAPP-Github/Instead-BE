package org.mainapp.domain.sns.twitter.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Twitter 로그인 후 리다이렉트 할 URL")
public record TwitterRedirectResponse(
	@Schema(description = "리다이렉트 url")
	String redirectUrl
) {
	public static TwitterRedirectResponse from(String redirectUrl) {
		return new TwitterRedirectResponse(redirectUrl);
	}
}
