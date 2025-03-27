package org.snsclient.twitter.client;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.snsclient.twitter.constants.ApiUrls;
import org.snsclient.twitter.dto.response.TwitterToken;
import org.snsclient.twitter.dto.response.TwitterUserInfoDto;
import org.snsclient.twitter.dto.response.TwitterUserResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import twitter4j.JSONObject;
import twitter4j.TwitterException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TwitterClient {
	private final WebClient webClient;

	/**
	 * Twitter API에 Media Upload 요청
	 */
	public String uploadMedia(MultiValueMap<String, Object> body, String accessToken) throws
		TwitterException {
		try {
			return webClient.post()
				.uri(URI.create(ApiUrls.TWITTER_MEDIA_UPLOAD_URL))
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.bodyValue(body)
				.retrieve()
				.bodyToMono(String.class)
				.block();

		} catch (Exception e) {
			log.error("Twitter Media 업로드 요청 중 에러 발생", e);
			throw new TwitterException("Twitter Media 업로드 요청 중 에러 발생: " + e.getMessage(), e);
		}
	}

	/**
	 * Twitter 본인 정보 요청
	 */
	public TwitterUserInfoDto getUserGetMeRequest(String userFields, String accessToken) throws
		TwitterException {
		try {
			URI uri = UriComponentsBuilder.fromHttpUrl(ApiUrls.TWITTER_GET_ME_URL)
				.queryParam("user.fields", userFields)
				.build()
				.encode()
				.toUri();
			TwitterUserResponse response = webClient.get()
				.uri(uri)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.retrieve()
				.bodyToMono(TwitterUserResponse.class)
				.block();

			return response.data(); // 내부 "data" 객체만 반환
		} catch (Exception e) {
			log.error("Twitter 사용자 정보 요청 중 에러 발생", e);
			throw new TwitterException("Twitter 사용자 정보 요청 중 에러 발생: " + e.getMessage(), e);
		}
	}

	/**
	 * Twitter Code를 통해서 AccessToken 발급 요청
	 */
	public TwitterToken getAccessTokenRequest(String clientId, String redirectUri, String code, String challenge) throws TwitterException {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("code", code);
		formData.add("grant_type", "authorization_code");
		formData.add("client_id", clientId);
		formData.add("redirect_uri", redirectUri);
		formData.add("code_verifier", challenge);


		try {
			String responseBody = webClient.post()
				.uri(URI.create(ApiUrls.TWITTER_GET_TOKEN_URL))
				.header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
				.body(BodyInserters.fromFormData(formData))
				.retrieve()
				.bodyToMono(String.class)
				.block();

			if (responseBody == null) {
				throw new TwitterException("Twitter AccessToken 발급 요청 응답이 비어 있습니다.");
			}

			JSONObject json = new JSONObject(responseBody);
			return TwitterToken.of(
				json.getString("access_token"),
				json.getString("refresh_token"),
				json.getLong("expires_in")
			);

		} catch (Exception e) {
			throw new TwitterException(e);
		}
	}

	/**
	 *  토큰 재발급
	 */
	public TwitterToken refreshTokenRequest(String refreshToken, String clientId) throws TwitterException {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("grant_type", "refresh_token");
		formData.add("refresh_token", refreshToken);
		formData.add("client_id", clientId);

		try {
			String responseBody = webClient.post()
				.uri(URI.create(ApiUrls.TWITTER_GET_TOKEN_URL))
				.header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
				.body(BodyInserters.fromFormData(formData))
				.retrieve()
				.bodyToMono(String.class)
				.block();

			if (responseBody == null) {
				throw new TwitterException("Twitter RefreshToken 요청 응답이 비어 있습니다.");
			}

			JSONObject json = new JSONObject(responseBody);
			return TwitterToken.of(
				json.getString("access_token"),
				json.getString("refresh_token"),
				json.getLong("expires_in")
			);

		} catch (Exception e) {
			throw new TwitterException(e);
		}
	}


	public String postTweet(String accessToken, String text, String[] mediaIds) throws TwitterException {
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("text", text);

		if (mediaIds != null) {
			requestBody.put("media", Map.of("media_ids", mediaIds));
		}
		try {
			String responseBody = webClient.post()
				.uri(URI.create(ApiUrls.TWITTER_POST_TWEET_URL))
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.bodyValue(requestBody)
				.retrieve()
				.bodyToMono(String.class)
				.block();

			if (responseBody == null) {
				throw new TwitterException("Twitter 응답이 비어 있습니다.");
			}

			JSONObject jsonResponse = new JSONObject(responseBody);

			if (jsonResponse.has("data")) {
				JSONObject data = jsonResponse.getJSONObject("data");
				return data.getString("id");
			} else {
				throw new TwitterException ("Twitter 응답에 'data' 객체가 없습니다.");
			}
		} catch (Exception e) {
			throw new TwitterException(e);
		}
	}
}
