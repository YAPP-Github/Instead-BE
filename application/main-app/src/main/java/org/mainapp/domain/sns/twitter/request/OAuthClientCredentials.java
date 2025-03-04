package org.mainapp.domain.sns.twitter.request;

public record OAuthClientCredentials(
	String clientId,
	String clientSecret
) {
	public static OAuthClientCredentials of(String clientId, String clientSecret) {
		return new OAuthClientCredentials(
			clientId,
			clientSecret
		);
	}
}
