package org.snsclient.twitter.service;

import org.snsclient.twitter.client.TwitterClient;
import org.snsclient.twitter.config.Twitter4jConfig;
import org.snsclient.twitter.dto.response.TwitterToken;
import org.snsclient.util.TwitterOauthUtil;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import twitter4j.TwitterException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TwitterAuthService {
	private final String[] scopes = {"media.write", "tweet.read", "tweet.write", "users.read", "offline.access"};
	private final Twitter4jConfig config;
	private final TwitterClient twitterClient;

	/**
	 * authorization url 생성 메서드
	 * @return authorization url
	 */
	public String getTwitterAuthorizationUrl(String userId, String clientId) {
		return createAuthorizeUrl(
			userId,
			clientId,
			config.getRedirectUri(),
			scopes,
			config.getChallenge());
	}

	private String createAuthorizeUrl(String userId, String clientId, String redirectUri, String[] scopes, String challenge) {
		String scope = String.join("%20", scopes);

		// Base64 URL-safe 인코딩
		String state = TwitterOauthUtil.encodeStateToBase64(userId, clientId);

		return "https://twitter.com/i/oauth2/authorize?response_type=code&" +
			"client_id=" + clientId + "&" +
			"redirect_uri=" + redirectUri + "&" +
			"scope=" + scope + "&" +
			"state=" + state + "&" +
			"code_challenge=" + challenge + "&" +
			"code_challenge_method=plain"  + "&" +
			"prompt=select_account";
	}

	/**
	 * Twitter AccessToken 발급 요청
	 */
	public TwitterToken getAccessToken(
		String clientId, String redirectUri, String code, String challenge
	) throws TwitterException {
		return twitterClient.getAccessTokenRequest (
			clientId,
			redirectUri,
			code,
			challenge
		);
	}
}
