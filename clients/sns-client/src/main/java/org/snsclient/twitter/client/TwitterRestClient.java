package org.snsclient.twitter.client;

import java.net.URI;

import org.snsclient.twitter.dto.response.TwitterUserInfoDto;
import org.snsclient.twitter.dto.response.TwitterUserResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import twitter4j.TwitterException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TwitterRestClient {
	private final WebClient webClient;

	/**
	 * Twitter API에 Media Upload 요청
	 */
	public String postMediaRequest(MultiValueMap<String, Object> body, String accessToken, String url) throws
		TwitterException {
		try {
			return webClient.post()
				.uri(URI.create(url))
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

	public TwitterUserInfoDto getUserGetMeRequest(String userFields, String accessToken, String url) throws
		TwitterException {
		try {
			URI uri = UriComponentsBuilder.fromHttpUrl(url)
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
	 * S3에서 이미지 다운로드
	 */
	public byte[] downloadImageFromS3(String presignedUrl) {
		return webClient.get()
			.uri(presignedUrl)
			.retrieve()
			.bodyToMono(byte[].class)
			.block();
	}
}
