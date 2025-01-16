package org.security.oauth2;

import java.io.IOException;

import org.domainmodule.user.service.AuthService;
import org.security.entity.CustomUserDetails;
import org.security.jwt.JwtProvider;
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
	private final AuthService authService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		CustomUserDetails customOAuth2User = (CustomUserDetails) authentication.getPrincipal();

		String accessToken = jwtProvider.generateAccessToken(customOAuth2User.getId());
		String refreshToken = jwtProvider.generateRegreshToken(customOAuth2User.getId());

		authService.saveRenewRefreshToken(customOAuth2User.getUser(), refreshToken);
		authService.setTokensInResponse(response, accessToken, refreshToken);

		// 응답 처리
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write("{\"accessToken\": \"" + accessToken + "\", \"refreshToken\": \"" + refreshToken + "\"}");
	}
}