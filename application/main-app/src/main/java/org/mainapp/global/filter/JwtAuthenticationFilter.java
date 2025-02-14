package org.mainapp.global.filter;

import java.io.IOException;

import org.mainapp.domain.token.exception.TokenErrorCode;
import org.mainapp.domain.token.service.TokenServiceImpl;
import org.mainapp.global.constants.HeaderConstants;
import org.mainapp.global.constants.WebSecurityURI;
import org.mainapp.global.error.CustomException;
import org.mainapp.global.util.JwtUtil;
import org.mainapp.global.util.ResponseUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final ResponseUtil responseUtil;
	private final TokenServiceImpl tokenService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		// String accessToken = jwtUtil.resolveToken(request, HeaderConstants.ACCESS_TOKEN_HEADER);
		//
		// if (accessToken == null) {
		// 	throw new CustomException(TokenErrorCode.ACCESS_TOKEN_NOT_FOUND);
		// }
		// String userId = jwtUtil.extractUserId(accessToken, true);
		//
		// // AccessToken이 만료되었을 때
		// if (!jwtUtil.isTokenValid(accessToken, true)) {
		//
		// 	String refreshToken = getRefreshTokenFromCookies(request);
		// 	if (refreshToken == null) {
		// 		throw new CustomException(TokenErrorCode.REFRESH_TOKEN_NOT_FOUND);
		// 	}
		// 	// RefreshToken 검증
		// 	userId = jwtUtil.extractUserId(refreshToken, false);
		// 	validateRefreshToken(userId, refreshToken);
		//
		// 	String newAccessToken = jwtUtil.generateAccessToken(userId);
		// 	String newRefreshToken = tokenService.generateRefreshToken(Long.parseLong(userId));
		// 	responseUtil.setTokensInResponse(response, newAccessToken, newRefreshToken);
		// }

		// TODO: 커밋하기 전 1로 변경
		String userId = "8";

		SecurityContextHolder.getContext().setAuthentication(jwtUtil.getAuthentication(userId));
		filterChain.doFilter(request, response);
	}

	/**
	 * 필터를 거치지 않는 URI를 설정
	 * @return 필터링 여부
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		// 정확히 매칭하도록 AntPathRequestMatcher를 제거하고 직접 비교
		return WebSecurityURI.PUBLIC_URIS.stream()
			.anyMatch(uri -> new AntPathRequestMatcher(uri).matches(request));
	}

	private String getRefreshTokenFromCookies(HttpServletRequest request) {
		if (request.getCookies() == null) {
			return null;
		}
		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals(HeaderConstants.REFRESH_TOKEN_HEADER)) {
				return cookie.getValue();
			}
		}
		return null;
	}

	private void validateRefreshToken(String userId, String refreshToken) {
		// RefreshToken 만료 검사
		if (!jwtUtil.isTokenValid(refreshToken, false)) {
			throw new CustomException(TokenErrorCode.REFRESH_TOKEN_EXPIRED);
		}
		// DB에 있는 RefreshToken 값과 비교
		tokenService.checkRefreshTokenMatch(userId, refreshToken);
	}
}
