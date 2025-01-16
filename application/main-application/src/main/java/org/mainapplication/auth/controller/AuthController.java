package org.mainapplication.auth.controller;

import org.mainapplication.token.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/yapp/auth")
public class AuthController {
	private final TokenService tokenService;

	// 토큰 재발급 API
	@PostMapping("/refresh")
	public ResponseEntity<String> refreshAccessToken(@CookieValue("RefreshToken") String refreshToken,
		HttpServletResponse response) {
		String newAccessToken = tokenService.reissueAccessToken(refreshToken, response);
		return ResponseEntity.ok(newAccessToken);
	}

}
