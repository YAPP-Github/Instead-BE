package org.mainapp.domain.v1.user.controller;

import org.mainapp.domain.v1.user.controller.response.UserInfoResponse;
import org.mainapp.domain.v1.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Deprecated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "유저에 대한 요청을 처리하는 API입니다.")
public class LegacyUserController {

	private final UserService userService;

	@GetMapping()
	public ResponseEntity<UserInfoResponse> getUsers() {
		return ResponseEntity.ok(userService.getUserInfo());
	}
}
