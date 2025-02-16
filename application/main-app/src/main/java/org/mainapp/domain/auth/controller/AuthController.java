package org.mainapp.domain.auth.controller;

import org.mainapp.domain.auth.service.AuthServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "로그인/회원가입에 대한 요청을 처리하는 API입니다.")
public class AuthController {
	private final AuthServiceImpl authService;

	@DeleteMapping("/logout")
	public ResponseEntity<Void> logout(@RequestHeader("Authorization") String accessToken) {
		authService.logout(accessToken);
		return ResponseEntity.noContent().build();
	}
}
