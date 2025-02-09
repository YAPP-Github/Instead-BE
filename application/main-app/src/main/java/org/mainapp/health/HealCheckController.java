package org.mainapp.health;

import org.mainapp.global.response.GlobalResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/common")
public class HealCheckController {

	@Operation(summary = "서버 상태 체크", security = {})
	@GetMapping("/health")
	public ResponseEntity<GlobalResponse> healthCheck() {
		return ResponseEntity.ok(GlobalResponse.success(200, "OK"));
	}
}
