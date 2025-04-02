package org.mainapp.domain.v1.auth.controller;

import org.mainapp.domain.v1.auth.service.AuthServiceImpl;
import org.mainapp.global.constants.HeaderConstants;
import org.mainapp.global.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "로그인/회원가입에 대한 요청을 처리하는 API입니다.")
public class AuthController {
	private final AuthServiceImpl authService;
	private final ResponseUtil responseUtil;

	@DeleteMapping("/logout")
	public ResponseEntity<Void> logout(@RequestHeader("Authorization") String accessToken,
		HttpServletResponse response) {
		authService.logout(accessToken);
		responseUtil.expireHttpOnlyCookie(response, HeaderConstants.REFRESH_TOKEN_HEADER);
		return ResponseEntity.noContent().build();
	}
}
