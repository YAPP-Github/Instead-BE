package org.snsclient.twitter.facade;

import org.snsclient.twitter.dto.response.TwitterToken;
import org.snsclient.twitter.dto.response.TwitterUserInfoDto;
import org.snsclient.twitter.service.Twitter4jService;
import org.snsclient.twitter.service.TwitterAuthService;
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
	private final Twitter4jService twitter4jService;
	private final TwitterAuthService twitterAuthService;

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
		return twitter4jService.getTwitterAuthorizationToken(code, clientId);
	}

	/**
	 * 토큰 만료 시 RefreshToken으로 AccessToken 갱신
	 */
	public TwitterToken refreshTwitterToken(String refreshToken) throws TwitterException {
		return twitter4jService.refreshTwitterToken(refreshToken);
	}

	/**
	 * 트윗 생성 API 호출 메서드
	 */
	public Long postTweet(String accessToken, String content, Long[] mediaIds) throws TwitterException {
		return twitter4jService.postTweet(accessToken, content, mediaIds);
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
