package org.snsclient.twitter.service;

import org.snsclient.twitter.dto.response.TwitterToken;
import org.snsclient.twitter.dto.response.TwitterUserInfoDto;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import twitter4j.TwitterException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwitterApiService {
	private final TwitterGetMeService twitterGetMeService;
	private final TwitterMediaUploadService twitterMediaUploadService;
	private final Twitter4jService twitter4jService;

	/**
	 * authorization url 생성 메서드
	 */
	public String getTwitterAuthorizationUrl(String userId) {
		String url = twitter4jService.getTwitterAuthorizationUrl();
		//TODO userID를 트위터 로그인 같이 감아보내는데 Base64인코딩해서 암호화하기

		// 기존 state 값을 교체하여 하나만 유지
		return url.replaceAll("&state=[^&]*", "") + "&state=" + userId + "&force_login=true";
	}

	/**
	 * 발급받은 code를 가지고 access token(2시간 동안 유효)을 발급받는 메서드
	 */
	public TwitterToken getTwitterAuthorizationToken(String code) {
		return twitter4jService.getTwitterAuthorizationToken(code);
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
		return twitterGetMeService.getUserInfo(accessToken);
	}
}
