package org.mainapplication.global.filter;

import java.io.IOException;

import org.mainapplication.global.constants.HeaderConstants;
import org.mainapplication.global.constants.WebSecurityURI;
import org.mainapplication.global.util.JwtUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String accessToken = jwtUtil.resolveToken(request, HeaderConstants.ACCESS_TOKEN_HEADER);
		if (accessToken == null) {
			throw new RuntimeException("ACCESS_TOKEN이 없습니다.");
		}

		boolean isAccessTokenValid = jwtUtil.isTokenValid(accessToken, true);
		// AccessToken이 만료
		if (!isAccessTokenValid ) {
			throw new RuntimeException("ACCESS_TOKEN이 만료되었습니다.");
		}

		String userId = jwtUtil.extractUserId(accessToken, true);
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
}

