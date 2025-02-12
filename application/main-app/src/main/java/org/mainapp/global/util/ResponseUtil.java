package org.mainapp.global.util;

import org.mainapp.global.constants.HeaderConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ResponseUtil {
	@Value("${jwt.refresh-token-expiration}")
	private int refreshTokenExpiration;

	public void setTokensInResponse(HttpServletResponse response, String accessToken, String refreshToken) {
		response.setHeader(HeaderConstants.ACCESS_TOKEN_HEADER, HeaderConstants.TOKEN_PREFIX + accessToken);
		createHttpOnlyCookie(response, refreshToken);
	}

	private void createHttpOnlyCookie(HttpServletResponse response, String refreshToken) {
		Cookie cookie = new Cookie(HeaderConstants.REFRESH_TOKEN_HEADER, refreshToken);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(refreshTokenExpiration / 1000);
		response.addCookie(cookie);
	}

	public void setContentType(HttpServletResponse response, String contentType) {
		response.setContentType(contentType);
	}
}
