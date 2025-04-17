package org.mainapp.domain.v1.sns.twitter;

import java.io.IOException;

import org.mainapp.domain.v1.sns.twitter.response.TwitterRedirectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/twitter")
@RequiredArgsConstructor
@Tag(name = "SNS - X(Twitter) API", description = "X(Twitter)와 관련된 API입니다.")
public class TwitterController {

	private final TwitterService twitterService;

	@Operation(summary = "X(Twitter) 계정 연결 API",
		description = "X(Twitter) 계정 연결을 위해 OAuth2 로그인 페이지로 이동하는 Url을 반환"
	)
	@GetMapping("/login")
	public ResponseEntity<TwitterRedirectResponse> redirectToTwitterAuth(
		@RequestHeader("Authorization") String accessToken
	) {
		String url = twitterService.createRedirectResponseV1(accessToken);
		return ResponseEntity.ok(TwitterRedirectResponse.from(url));
	}

	@Operation(summary = "X(Twitter) 로그인 성공 후 Redirect", description = "/twitter/login 요청 후 자동으로 리다이렉트되는 api입니다 (직접 호출 X)")
	@GetMapping("/success")
	public void handleTwitterLoginCallback(
		@RequestParam String code,
		@RequestParam String state,
		HttpServletResponse response
	) throws IOException {
		String redirectUrl = twitterService.loginOrRegister(code, state);
		response.sendRedirect(redirectUrl);
	}
}

