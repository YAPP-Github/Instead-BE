package org.snsclient.twitter.service;

import org.snsclient.twitter.client.TwitterClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import twitter4j.TwitterException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwitterTweetService {
	private final TwitterClient twitterClient;

	/**
	 * 트윗 생성 API 호출 메서드
	 * @param content 트윗 내용
	 */
	public String postTweet(String accessToken, String content, String[] mediaIds) throws TwitterException {
		return twitterClient.postTweet(accessToken, content, mediaIds);
	}
}
