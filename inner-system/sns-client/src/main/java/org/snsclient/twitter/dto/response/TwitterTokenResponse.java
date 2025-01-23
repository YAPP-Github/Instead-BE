package org.snsclient.twitter.dto.response;

public record TwitterTokenResponse(
	String accessToken,
	String refreshToken,
	long expiresIn
) {
	public static TwitterTokenResponse fromFields(String accessToken, String refreshToken, long expiresIn) {
		return new TwitterTokenResponse(accessToken, refreshToken, expiresIn);
	}

	public static TwitterTokenResponse fromTokens(String accessToken, String refreshToken) {
		return new TwitterTokenResponse(accessToken, refreshToken, 0);
	}
}

