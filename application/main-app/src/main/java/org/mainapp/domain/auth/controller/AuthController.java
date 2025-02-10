package org.mainapp.domain.auth.controller;

import org.mainapp.domain.token.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
	private final TokenService tokenService;

	//TODO 배포 후 Swagger에서 안보이도록 hidden 처리
	@Operation(summary = "JWT 토큰 재발급", description = "RefreshToken을 통해 AccessToken 재발급")
	@PostMapping("/refresh")
	public ResponseEntity<String> refreshAccessToken(@CookieValue("RefreshToken") String refreshToken,
		HttpServletResponse response) {
		String newAccessToken = tokenService.reissueAccessToken(refreshToken, response);
		return ResponseEntity.ok(newAccessToken);
	}
}
