package org.mainapp.global.filter;

import java.io.IOException;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CsrfHeaderFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
		if (csrfToken != null) {
			String token = csrfToken.getToken();

			// 응답 헤더에 CSRF 토큰 추가
			response.setHeader("X-XSRF-TOKEN", token);

			// 쿠키에도 동일한 값 설정
			Cookie csrfCookie = new Cookie("XSRF-TOKEN", token);
			csrfCookie.setPath("/");
			csrfCookie.setHttpOnly(false);  // JavaScript에서 읽을 수 있도록 설정
			csrfCookie.setSecure(true);  // HTTPS에서만 사용 가능 (개발 환경에서는 false로 설정 가능)
			csrfCookie.setMaxAge(-1); // 세션 쿠키 (브라우저 종료 시 삭제)
			response.addCookie(csrfCookie);
		}

		filterChain.doFilter(request, response);
	}
}