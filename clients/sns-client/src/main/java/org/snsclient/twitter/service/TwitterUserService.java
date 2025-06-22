package org.snsclient.twitter.service;

import org.snsclient.twitter.client.TwitterClient;
import org.snsclient.twitter.dto.response.TwitterUserInfoDto;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import twitter4j.TwitterException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwitterUserService {

	private final TwitterClient twitterClient;

	/**
	 * X로 부터 유저 본인의 정보 받아오기
	 * @return 트위터 유저 기본 정보
	 * https://docs.x.com/x-api/users/user-lookup-me
	 */
	public TwitterUserInfoDto getUserInfo(String accessToken) throws TwitterException {
		return twitterClient.getUserGetMeRequest(
			"description,profile_image_url,subscription_type"
			, accessToken);
	}
}
