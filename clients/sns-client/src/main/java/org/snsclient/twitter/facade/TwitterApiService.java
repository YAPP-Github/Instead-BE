package org.snsclient.twitter.facade;

import org.snsclient.twitter.dto.response.TwitterToken;
import org.snsclient.twitter.dto.response.TwitterUserInfoDto;
import org.snsclient.twitter.service.TwitterAuthService;
import org.snsclient.twitter.service.TwitterTweetService;
import org.snsclient.twitter.service.TwitterUserService;
import org.snsclient.twitter.service.TwitterMediaUploadService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import twitter4j.TwitterException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwitterApiService {
	private final TwitterUserService twitterUserService;
	private final TwitterMediaUploadService twitterMediaUploadService;
	private final TwitterAuthService twitterAuthService;
	private final TwitterTweetService twitterTweetService;

	/**
	 * authorization url 생성 메서드
	 */
	public String getTwitterAuthorizationUrl(String userId, String clientId) {
		return twitterAuthService.getTwitterAuthorizationUrl(userId, clientId);
	}

	/**
	 * 발급받은 code를 가지고 access token(2시간 동안 유효)을 발급받는 메서드
	 */
	public TwitterToken getTwitterAuthorizationToken(String code, String clientId) {
		return twitterAuthService.getTwitterAuthorizationToken(code, clientId);
	}

	/**
	 * 토큰 만료 시 RefreshToken으로 AccessToken 갱신
	 */
	public TwitterToken refreshTwitterToken(String refreshToken, String clientId) throws TwitterException {
		return twitterAuthService.refreshTwitterToken(refreshToken, clientId);
	}

	/**
	 * 글 생성 API 호출 메서드
	 */
	public String postTweet(String accessToken, String content, String[] mediaIds) throws TwitterException {
		return twitterTweetService.postTweet(accessToken, content, mediaIds);
	}

	/**
	 * Twitter 미디어 업로드
	 */
	public String uploadMedia(String presignedUrl, String accessToken) throws TwitterException {
		return twitterMediaUploadService.uploadMedia(presignedUrl, accessToken);
	}

	/**
	 * X로 부터 유저 본인의 정보 받아오기
	 */
	public TwitterUserInfoDto getUserInfo(String accessToken) throws TwitterException {
		return twitterUserService.getUserInfo(accessToken);
	}
}
