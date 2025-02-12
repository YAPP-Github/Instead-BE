package org.mainapp.global.util;

import java.time.Duration;

import org.mainapp.global.constants.HeaderConstants;
import org.mainapp.global.constants.JwtProperties;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ResponseUtil {
	private final JwtProperties jwtProperties;

	public void setTokensInResponse(HttpServletResponse response, String accessToken, String refreshToken) {
		response.setHeader(HeaderConstants.ACCESS_TOKEN_HEADER, HeaderConstants.TOKEN_PREFIX + accessToken);
		createHttpOnlyCookie(response, refreshToken);
	}

	private void createHttpOnlyCookie(HttpServletResponse response, String refreshToken) {
		long refreshTokenExpirationSC = Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMS()).toSeconds();

		Cookie cookie = new Cookie(HeaderConstants.REFRESH_TOKEN_HEADER, refreshToken);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge((int) refreshTokenExpirationSC);
		response.addCookie(cookie);
	}

	public void setContentType(HttpServletResponse response, String contentType) {
		response.setContentType(contentType);
	}
}
