package org.snsclient.twitter.service;

import org.snsclient.twitter.config.TwitterConfig;
import org.snsclient.twitter.dto.response.TwitterTokenResponse;
import org.snsclient.twitter.dto.response.TwitterUserInfoDto;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import twitter4j.OAuth2TokenProvider;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterV2;
import twitter4j.TwitterV2ExKt;
import twitter4j.UsersResponse;
import twitter4j.auth.OAuth2Authorization;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

@Service
@RequiredArgsConstructor
public class TwitterApiService {
	private final TwitterConfig config;
	private final String[] scopes = {"tweet.read", "tweet.write", "users.read", "offline.access"};
	private final OAuth2TokenProvider twitterOAuth2TokenProvider;

	/**
	 * authorization url 생성 메서드
	 * @return authorization url
	 */
	public String getTwitterAuthorizationUrl() {
		return twitterOAuth2TokenProvider.createAuthorizeUrl(config.getClientId(), config.getRedirectUri(), scopes,
			config.getChallenge());
	}

	/**
	 * 발급받은 code를 가지고 access token(2시간 동안 유효)을 발급받는 메서드
	 * @param code 발급받은 code (10분간 유효)
	 * @return access token
	 */
	public TwitterTokenResponse getTwitterAuthorizationToken(String code) {
		OAuth2TokenProvider.Result result = twitterOAuth2TokenProvider.getAccessToken(
			config.getClientId(),
			config.getRedirectUri(),
			code,
			config.getChallenge()
		);

		validateTokenResult(result);

		return TwitterTokenResponse.fromFields(
			result.getAccessToken(),
			result.getRefreshToken(),
			result.getExpiresIn()
		);
	}

	private void validateTokenResult(OAuth2TokenProvider.Result result) {
		if (result == null) {
			throw new IllegalStateException("OAuth2TokenProvider 값이 존재하지 않습니다.");
		}
		if (result.getExpiresIn() <= 0) {
			throw new IllegalArgumentException("ExpireIn이 잘못된 값입니다.");
		}
		validateRefreshTokenProcess(result);
	}

	private boolean isNullOrBlank(String value) {
		return value == null || value.isBlank();
	}

	/**
	 * TwitterV2 클라이언트 인스턴스를 생성해 반환하는 메서드
	 * @param accessToken accessToken
	 * @return TwitterV2 인스턴스
	 */
	private TwitterV2 createTwitterV2(String accessToken) {
		Configuration configuration = new ConfigurationBuilder()
			.setOAuthConsumerKey(config.getClientId())
			.setOAuthConsumerSecret(config.getClientSecret())
			.build();

		OAuth2Authorization auth = new OAuth2Authorization(configuration);
		auth.setOAuth2Token(new OAuth2Token("bearer", accessToken));

		Twitter twitter = new TwitterFactory(configuration).getInstance(auth);

		return TwitterV2ExKt.getV2(twitter);
	}

	/**
	 * 토큰 만료 시 RefreshToken으로 AccessToken 갱신
	 * @param refreshToken 기존 Twitter RefreshToken
	 */
	public TwitterTokenResponse refreshTwitterToken(String refreshToken) {
		OAuth2TokenProvider.Result result = twitterOAuth2TokenProvider.refreshToken(config.getClientId(), refreshToken);
		validateRefreshTokenProcess(result);
		return TwitterTokenResponse.fromTokens(result.getAccessToken(), result.getRefreshToken());
	}

	private void validateRefreshTokenProcess(OAuth2TokenProvider.Result result) {
		if (result == null) {
			throw new IllegalStateException("OAuth2TokenProvider 값이 존재하지 않습니다.");
		}
		if (isNullOrBlank(result.getAccessToken())) {
			throw new IllegalArgumentException("Twitter AccessToken 발급에 실패하였습니다.");
		}
		if (isNullOrBlank(result.getRefreshToken())) {
			throw new IllegalArgumentException("Twitter RefreshToken 발급에 실패하였습니다.");
		}
	}

	public TwitterUserInfoDto getUserInfo(String accessToken, String refreshToken) {
		try {
			return fetchUserInfo(accessToken);
		} catch (TwitterException e) {
			return handleTokenRefreshAndRetry(refreshToken);
		}
	}

	private TwitterUserInfoDto fetchUserInfo(String accessToken) throws TwitterException {
		TwitterV2 twitterV2 = createTwitterV2(accessToken);
		UsersResponse usersResponse = twitterV2.getMe("", null, "description");
		return mapToUserInfoDto(usersResponse);
	}

	private TwitterUserInfoDto mapToUserInfoDto(UsersResponse usersResponse) {
		return usersResponse.getUsers()
			.stream()
			.findFirst()  //TODO twitter.getMe가 여러 유저를 리턴가능하기에 설정
			.map(TwitterUserInfoDto::fromTwitterUser)
			.orElseThrow(() -> new IllegalStateException("X 유저 정보가 없습니다."));
	}

	private TwitterUserInfoDto handleTokenRefreshAndRetry(String refreshToken) {
		try {
			TwitterTokenResponse newTokenResponse = refreshTwitterToken(refreshToken);
			return fetchUserInfo(newTokenResponse.accessToken());
		} catch (TwitterException e) {
			throw new RuntimeException("토큰 갱신 및 사용자 정보를 가져오는 데 실패했습니다.", e);
		}
	}
}
