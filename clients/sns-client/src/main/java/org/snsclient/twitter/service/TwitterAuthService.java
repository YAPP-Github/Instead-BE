package org.snsclient.twitter.service;

import org.snsclient.twitter.client.TwitterClient;
import org.snsclient.twitter.config.TwitterConfig;
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
	private final TwitterConfig config;
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
	 * 발급받은 code를 가지고 access token(2시간 동안 유효)을 발급받는 메서드
	 * @param code 발급받은 code (10분간 유효)
	 * @return access token
	 */
	public TwitterToken getTwitterAuthorizationToken(String code, String clientId) {
		try {
			return twitterClient.getAccessTokenRequest(
				clientId,
				config.getRedirectUri(),
				code,
				config.getChallenge()
			);
		} catch (Exception e) {
			log.error("Twitter Token 발급 API 호출 중 오류 발생: {}", e.getMessage());
			throw new RuntimeException("Twitter Token 발급 API 호출 중 오류 발생", e);
		}
	}

	/**
	 * 토큰 만료 시 RefreshToken으로 AccessToken 재발급 요청
	 * @param refreshToken 기존 Twitter RefreshToken
	 */
	public TwitterToken refreshTwitterToken(String refreshToken, String clientId) throws TwitterException {
		try {
			return twitterClient.refreshTokenRequest(refreshToken, clientId);
		} catch (Exception e) {
			log.error("Twitter Token 재발급 호출 중 오류 발생: {}", e.getMessage());
			throw new TwitterException("Twitter RefreshToken 갱신 중 오류 발생", e);
		}
	}
}
