package org.snsclient.twitter.service;

import org.snsclient.twitter.client.TwitterRestClient;
import org.snsclient.twitter.dto.response.TwitterUserInfoDto;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import twitter4j.TwitterException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwitterGetMeService {

	private final TwitterRestClient twitterRestClient;

	private final String TWITTER_GET_ME_URL = "https://api.x.com/2/users/me";

	/**
	 * X로 부터 유저 본인의 정보 받아오기
	 * @param accessToken
	 * @return 트위터 유저 기본 정보
	 */
	public TwitterUserInfoDto getUserInfo(String accessToken) throws TwitterException {
		return twitterRestClient.getUserGetMeRequest("description,profile_image_url,subscription_type", accessToken, TWITTER_GET_ME_URL);
	}
}
