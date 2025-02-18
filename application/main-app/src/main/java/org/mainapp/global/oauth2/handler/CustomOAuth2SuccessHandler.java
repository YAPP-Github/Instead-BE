package org.mainapp.global.oauth2.handler;

import java.io.IOException;

import org.mainapp.global.constants.UrlConstants;
import org.mainapp.global.oauth2.CustomUserDetails;
import org.mainapp.global.util.JwtUtil;
import org.mainapp.global.util.ResponseUtil;
import org.mainapp.domain.token.service.TokenService;
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
	private final ResponseUtil responseUtil;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		CustomUserDetails customOAuth2User = (CustomUserDetails) authentication.getPrincipal();

		String accessToken = jwtUtil.generateAccessToken(customOAuth2User.getId());
		String refreshToken = tokenService.getRefreshToken(Long.parseLong(customOAuth2User.getId()));

		responseUtil.setTokensInResponse(response, accessToken, refreshToken);

		// 응답 처리
		responseUtil.setContentType(response, "application/json;charset=UTF-8");
		response.sendRedirect(UrlConstants.LOCAL_DOMAIN_URL  + "/api/auth/callback/google" );
	}
}