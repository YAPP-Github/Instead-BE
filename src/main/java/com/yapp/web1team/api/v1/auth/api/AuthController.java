package com.yapp.web1team.api.v1.auth.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yapp.web1team.api.v1.auth.dto.request.AuthRequest;
import com.yapp.web1team.api.v1.auth.dto.response.SignInResponse;
import com.yapp.web1team.api.v1.auth.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
	private final AuthService authService;

	// 회원가입 API
	@PostMapping("/sign-up")
	public String signUp(
		@RequestBody AuthRequest request
	) {
		authService.signUp(request);
		return "회원가입에 성공하였습니다!";
	}

	// 로그인 API
	@PostMapping("/sign-in")
	public ResponseEntity<SignInResponse> login(
		@RequestBody AuthRequest request,
		HttpServletResponse response
	) {
		return ResponseEntity.ok(authService.signIn(request, response));
	}

	// 토큰 재발급 API
	@PostMapping("/refresh")
	public ResponseEntity<String> refreshAccessToken(@CookieValue("Refresh-Token") String refreshToken,
		HttpServletResponse response) {
		String newAccessToken = authService.reissueAccessToken(refreshToken, response);
		return ResponseEntity.ok(newAccessToken);
	}

	// 인증 테스트 API
	@GetMapping("/test")
	public ResponseEntity<String> test() {
		return ResponseEntity.ok("테스트 입니다");
	}

	public String handleOAuth2Login(@AuthenticationPrincipal OAuth2User oAuth2User) {
		return "redirect:/api/v1/auth/success";
	}

	@GetMapping("/success")
	public String success() {
		return "OAuth2 login successful!";
	}

}