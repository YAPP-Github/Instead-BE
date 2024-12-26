package com.yapp.web1team.domain.auth.dto.response;

public record SignInResponse(
	String account,
	String accessToken,
	String refreshToken

) {
	public static SignInResponse of(String account,String accessToken, String refreshToken) {
		return new SignInResponse(account, accessToken, refreshToken);
	}
}
