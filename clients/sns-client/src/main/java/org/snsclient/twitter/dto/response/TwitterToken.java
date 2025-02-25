package org.snsclient.twitter.dto.response;

public record TwitterToken(
	String accessToken,
	String refreshToken,
	long expiresIn
) {
	public static TwitterToken of(String accessToken, String refreshToken, long expiresIn) {
		return new TwitterToken(accessToken, refreshToken, expiresIn);
	}
}

