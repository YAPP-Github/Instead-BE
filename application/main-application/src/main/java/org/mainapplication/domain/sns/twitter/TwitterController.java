package org.mainapplication.domain.sns.twitter;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/twitter")
@RequiredArgsConstructor
public class TwitterController {

	private final TwitterService twitterService;

	@GetMapping("/login")
	public ResponseEntity<Void> redirectToTwitterAuth() {
		return twitterService.createRedirectResponse();
	}

	//TODO 외부에서 접근 못하도록 설정
	@GetMapping("/success")
	public ResponseEntity<Map<String, String>> handleTwitterLoginCallback(
		@RequestParam String code
	) {
		twitterService.loginOrRegister(code);
		Map<String, String> response = new HashMap<>();
		response.put("message", "X 로그인 성공");
		return ResponseEntity.ok(response);
	}
}
