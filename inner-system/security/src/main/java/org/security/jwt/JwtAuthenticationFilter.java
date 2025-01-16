package org.security.jwt;

import java.io.IOException;

import org.security.constants.HeaderConstants;
import org.security.constants.WebSecurityURI;
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

	private final JwtProvider jwtProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String accessToken = jwtProvider.resolveToken(request, HeaderConstants.ACCESS_TOKEN_HEADER);

		if (accessToken == null) {
			throw new RuntimeException("ACCESS_TOKEN이 없습니다.");
		}

		boolean isAccessTokenValid = jwtProvider.isTokenValid(accessToken, true);

		// AccessToken이 만료
		if (!isAccessTokenValid ) {
			throw new RuntimeException("ACCESS_TOKEN이 만료되었습니다.");
		}

		String userId = jwtProvider.extractUserId(accessToken, true);

		// 유저의 id로만 인증객체 생성
		SecurityContextHolder.getContext().setAuthentication(jwtProvider.getAuthentication(userId));

		filterChain.doFilter(request, response); // 다음 필터로 넘기기
	}


	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String requestURI = request.getRequestURI();

		// 정적 리소스 경로를 필터링하지 않음
		if (requestURI.matches(".*\\.(css|js|png|jpg|jpeg|svg|ico)$")) {
			return true; // 필터 건너뛰기
		}

		// 정확히 매칭하도록 AntPathRequestMatcher를 제거하고 직접 비교
		boolean shouldNotFilter = WebSecurityURI.PUBLIC_URIS.stream()
			.anyMatch(uri -> new AntPathRequestMatcher(uri).matches(request));

		System.out.println("Request URI: " + requestURI + ", Should not filter: " + shouldNotFilter);

		return shouldNotFilter;
	}
}

