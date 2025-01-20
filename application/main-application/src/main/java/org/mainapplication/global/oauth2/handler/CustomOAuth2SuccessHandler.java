package org.mainapplication.global.oauth2.handler;

import java.io.IOException;

import org.mainapplication.global.oauth2.CustomUserDetails;
import org.mainapplication.global.util.JwtUtil;
import org.mainapplication.global.util.ResponseUtil;
import org.mainapplication.domain.token.service.TokenService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final JwtUtil jwtUtil;
	private final TokenService tokenService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		CustomUserDetails customOAuth2User = (CustomUserDetails) authentication.getPrincipal();

		String accessToken = jwtUtil.generateAccessToken(customOAuth2User.getId());
		String refreshToken = jwtUtil.generateRegreshToken(customOAuth2User.getId());

		tokenService.saveRenewRefreshToken(customOAuth2User.getUser(), refreshToken);
		ResponseUtil.setTokensInResponse(response, accessToken, refreshToken);

		// 응답 처리
		ResponseUtil.setContentType(response, "application/json;charset=UTF-8");
		//TODO 이미 헤더와 쿠키에 값을 넣어서 리턴하므로 상의 후 제거
		response.getWriter().write("{\"accessToken\": \"" + accessToken + "\", \"refreshToken\": \"" + refreshToken + "\"}");
	}
}