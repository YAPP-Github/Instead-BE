package org.mainapplication.global.filter;

import java.io.IOException;

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
public class TestAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String userId = "1";
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
