package org.mainapplication.global.oauth2.handler;

import java.io.IOException;

import org.mainapplication.global.oauth2.CustomUserDetails;
import org.mainapplication.global.jwt.JwtProvider;
import org.mainapplication.global.util.ResponseUtil;
import org.mainapplication.token.service.TokenService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final JwtProvider jwtProvider;
	private final TokenService tokenService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		CustomUserDetails customOAuth2User = (CustomUserDetails) authentication.getPrincipal();

		String accessToken = jwtProvider.generateAccessToken(customOAuth2User.getId());
		String refreshToken = jwtProvider.generateRegreshToken(customOAuth2User.getId());

		tokenService.saveRenewRefreshToken(customOAuth2User.getUser(), refreshToken);
		ResponseUtil.setTokensInResponse(response, accessToken, refreshToken);

		// 응답 처리
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write("{\"accessToken\": \"" + accessToken + "\", \"refreshToken\": \"" + refreshToken + "\"}");
	}
}