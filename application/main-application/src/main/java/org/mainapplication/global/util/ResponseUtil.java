package org.mainapplication.global.util;

import org.mainapplication.global.constants.HeaderConstants;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class ResponseUtil {
	private ResponseUtil() {
	}

	public static void setTokensInResponse(HttpServletResponse response, String accessToken, String refreshToken) {
		response.setHeader(HeaderConstants.ACCESS_TOKEN_HEADER, HeaderConstants.TOKEN_PREFIX + accessToken);
		createHttpOnlyCookie(response, refreshToken);
	}

	private static void createHttpOnlyCookie(HttpServletResponse response, String refreshToken) {
		Cookie cookie = new Cookie(HeaderConstants.REFRESH_TOKEN_HEADER, refreshToken);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(7 * 24 * 60 * 60); //TODO 쿠키 기간 설정하기 유효기간 7일
		response.addCookie(cookie);
	}

	public static void setContentType(HttpServletResponse response, String contentType) {
		response.setContentType(contentType);
	}
}
