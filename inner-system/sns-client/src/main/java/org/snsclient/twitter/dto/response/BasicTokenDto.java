package org.snsclient.twitter.dto.response;

public record BasicTokenDto(
	String accessToken,
	String refreshToken
) {
	public static BasicTokenDto of(String accessToken, String refreshToken) {
		return new BasicTokenDto(accessToken, refreshToken);
	}
}
