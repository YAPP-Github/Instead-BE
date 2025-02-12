package org.snsclient.twitter.service;

import org.snsclient.twitter.config.TwitterConfig;
import org.snsclient.twitter.dto.response.TwitterToken;
import org.snsclient.twitter.dto.response.TwitterUserInfoDto;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import twitter4j.CreateTweetResponse;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class TwitterApiService {
	private final TwitterConfig config;
	private final String[] scopes = {"media.write", "tweet.read", "tweet.write", "users.read", "offline.access"};
	private final OAuth2TokenProvider twitterOAuth2TokenProvider;

	/**
	 * authorization url 생성 메서드
	 * @return authorization url
	 */
	public String getTwitterAuthorizationUrl() {
		return twitterOAuth2TokenProvider.createAuthorizeUrl(
			config.getClientId(),
			config.getRedirectUri(),
			scopes,
			config.getChallenge());
	}

	/**
	 * 발급받은 code를 가지고 access token(2시간 동안 유효)을 발급받는 메서드
	 * @param code 발급받은 code (10분간 유효)
	 * @return access token
	 */
	public TwitterToken getTwitterAuthorizationToken(String code) {
		try {
			OAuth2TokenProvider.Result result = twitterOAuth2TokenProvider.getAccessToken(
				config.getClientId(),
				config.getRedirectUri(),
				code,
				config.getChallenge()
			);

			validateTokenResult(result);

			return TwitterToken.of(
				result.getAccessToken(),
				result.getRefreshToken(),
				result.getExpiresIn()
			);
		} catch (Exception e) {
			log.error("Twitter Token 발급 API 호출 중 오류 발생: {}", e.getMessage());
			throw new RuntimeException("Twitter Token 발급 API 호출 중 오류 발생", e);
		}
	}

	private void validateTokenResult(OAuth2TokenProvider.Result result) {
		if (result == null) {
			throw new IllegalStateException("Twitter OAuth2TokenProvider 값이 존재하지 않습니다.");
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
	private TwitterV2 createTwitterV2(String accessToken) throws TwitterException {
		try {
			Configuration configuration = new ConfigurationBuilder()
				.setOAuthConsumerKey(config.getClientId())
				.setOAuthConsumerSecret(config.getClientSecret())
				.build();

			OAuth2Authorization auth = new OAuth2Authorization(configuration);
			auth.setOAuth2Token(new OAuth2Token("bearer", accessToken));

			Twitter twitter = new TwitterFactory(configuration).getInstance(auth);

			return TwitterV2ExKt.getV2(twitter);
		} catch (Exception e) {
			log.error("TwitterV2 클라이언트 생성 중 오류 발생: {}", e.getMessage());
			throw new TwitterException("TwitterV2 클라이언트 생성 중 오류 발생", e);
		}
	}

	/**
	 * 토큰 만료 시 RefreshToken으로 AccessToken 갱신
	 * @param refreshToken 기존 Twitter RefreshToken
	 */
	public TwitterToken refreshTwitterToken(String refreshToken) throws TwitterException {
		try {
			final String clientId = config.getClientId();
			OAuth2TokenProvider.Result result = twitterOAuth2TokenProvider.refreshToken(clientId, refreshToken);
			validateRefreshTokenProcess(result);
			return TwitterToken.of(result.getAccessToken(), result.getRefreshToken(), result.getExpiresIn());
		} catch (Exception e) {
			log.error("Twitter Token 재발급 호출 중 오류 발생: {}", e.getMessage());
			throw new TwitterException("Twitter RefreshToken 갱신 중 오류 발생", e);
		}
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

	/**
	 * X로 부터 유저 본인의 정보 받아오기 (401시 토큰 재발급 후 요청)
	 * @param accessToken
	 * @return 트위터 유저 기본 정보
	 */
	public TwitterUserInfoDto getUserInfo(String accessToken) throws TwitterException {
		TwitterV2 twitterV2 = createTwitterV2(accessToken);
		UsersResponse usersResponse = twitterV2.getMe("", null, "description,profile_image_url");
		return mapToUserInfoDto(usersResponse);
	}

	private TwitterUserInfoDto mapToUserInfoDto(UsersResponse usersResponse) throws TwitterException {
		return usersResponse.getUsers()
			.stream()
			.findFirst()  //twitter.getMe가 여러 유저를 리턴가능하기에 설정
			.map(TwitterUserInfoDto::fromTwitterUser)
			.orElseThrow(() -> new TwitterException("X 유저 정보가 없습니다."));
	}

	/**
	 * 트윗 생성 API 호출 메서드
	 * @param content 트윗 내용
	 */
	public Long postTweet(String accessToken, String content, Long[] mediaIds) throws TwitterException {
		TwitterV2 twitterV2 = createTwitterV2(accessToken);
		CreateTweetResponse tweetResponse = twitterV2.createTweet(null, null, null, mediaIds, null, null, null, null, null, null, null, content);
		return tweetResponse.getId();
	}
}
