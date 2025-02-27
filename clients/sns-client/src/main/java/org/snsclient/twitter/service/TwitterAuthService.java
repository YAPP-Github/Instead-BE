package org.snsclient.twitter.service;

import org.snsclient.twitter.config.Twitter4jConfig;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TwitterAuthService {
	private final String[] scopes = {"media.write", "tweet.read", "tweet.write", "users.read", "offline.access"};
	private final Twitter4jConfig config;

	/**
	 * authorization url 생성 메서드
	 * @return authorization url
	 */
	public String getTwitterAuthorizationUrl(String userId) {
		return createAuthorizeUrl(
			userId,
			config.getClientId(),
			config.getRedirectUri(),
			scopes,
			config.getChallenge());
	}

	private String createAuthorizeUrl(String userId, String clientId, String redirectUri, String[] scopes, String challenge) {
		if (challenge == null || challenge.isEmpty()) {
			challenge = "challenge";
		}

		String scope = String.join("%20", scopes);

		return "https://twitter.com/i/oauth2/authorize?response_type=code&" +
			"client_id=" + clientId + "&" +
			"redirect_uri=" + redirectUri + "&" +
			"scope=" + scope + "&" +
			"state=" + userId + "&" +
			"code_challenge=" + challenge + "&" +
			"code_challenge_method=plain"  + "&" +
			"prompt=select_account";
	}

}
