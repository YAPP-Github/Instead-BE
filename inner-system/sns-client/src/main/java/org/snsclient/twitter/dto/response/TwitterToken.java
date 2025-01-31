package org.snsclient.twitter.dto.response;

//TODO interface로 twitter, thread, instagram도 가능하도록 변경하기
public record TwitterToken(
	String accessToken,
	String refreshToken,
	long expiresIn
) {
	public static TwitterToken fromFields(String accessToken, String refreshToken, long expiresIn) {
		return new TwitterToken(accessToken, refreshToken, expiresIn);
	}
}

