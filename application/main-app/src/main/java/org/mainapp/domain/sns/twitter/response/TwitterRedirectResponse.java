package org.mainapp.domain.sns.twitter.response;

public record TwitterRedirectResponse(
	String redirectUrl
) {
	public static TwitterRedirectResponse from(String redirectUrl) {
		return new TwitterRedirectResponse(redirectUrl);
	}
}
