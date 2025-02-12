package org.mainapp.global.util;

import org.mainapp.global.constants.HeaderConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ResponseUtil {

	@Value("${cookie.token.access-expiration}")
	private int accessTokenExpiration;

	@Value("${cookie.token.refresh-expiration}")
	private int refreshTokenExpiration;

	public void setTokensInResponse(HttpServletResponse response, String accessToken, String refreshToken) {
		createHttpOnlyCookie(response, HeaderConstants.ACCESS_TOKEN_HEADER, accessToken, accessTokenExpiration);
		createHttpOnlyCookie(response, HeaderConstants.REFRESH_TOKEN_HEADER, refreshToken, refreshTokenExpiration);
	}

	private void createHttpOnlyCookie(HttpServletResponse response, String name, String token, int maxAge) {
		Cookie cookie = new Cookie(name, token);
		cookie.setHttpOnly(true); // xss 방지
		//TODO 서비스 배포 시 주석 제거하기
		// cookie.setSecure(true); // https에서만 사용가능
		cookie.setPath("/");
		cookie.setMaxAge(maxAge);

		response.addCookie(cookie);
		// CSRF 방어를 위한 SameSite 설정
		// TODO 서비스 배포 SameSite=None으로 변경해야함
		response.addHeader("Set-Cookie", String.format(
			"%s=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=Lax",
			name, token, maxAge));
	}

	public void setContentType(HttpServletResponse response, String contentType) {
		response.setContentType(contentType);
	}
}