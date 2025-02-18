package org.mainapp.domain.sns.twitter;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/twitter")
@RequiredArgsConstructor
@Tag(name = "SNS - X(Twitter) API", description = "X(Twitter)와 관련된 API입니다.")
public class TwitterController {

	private final TwitterService twitterService;

	@Operation(summary = "X(Twitter) 계정 연결 API", description = "X(Twitter) 계정 연결을 위해 OAuth2 로그인 페이지로 이동하는 API입니다.")
	@GetMapping("/login")
	public ResponseEntity<Void> redirectToTwitterAuth() {
		return twitterService.createRedirectResponse();
	}

	@GetMapping("/success")
	public void handleTwitterLoginCallback(
		@RequestParam String code,
		HttpServletResponse response
	) throws IOException {
		String redirectUrl = twitterService.loginOrRegister(code);
		response.sendRedirect(redirectUrl);
	}
}
