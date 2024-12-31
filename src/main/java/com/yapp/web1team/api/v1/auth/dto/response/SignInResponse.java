package com.yapp.web1team.api.v1.auth.dto.response;

public record SignInResponse(
	String account,
	String accessToken,
	String refreshToken

) {
	public static SignInResponse of(String account,String accessToken, String refreshToken) {
		return new SignInResponse(account, accessToken, refreshToken);
	}
}
