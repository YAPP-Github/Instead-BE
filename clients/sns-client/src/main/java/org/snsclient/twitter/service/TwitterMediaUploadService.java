package org.snsclient.twitter.service;

import java.util.ArrayList;

import org.snsclient.twitter.client.TwitterRestClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import twitter4j.TwitterException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwitterMediaUploadService {

	private final ObjectMapper objectMapper;
	private final TwitterRestClient twitterRestClient;

	private final String TWITTER_MEDIA_UPLOAD_URL = "https://api.x.com/2/media/upload";

	/**
	 * Presigned URL을 사용해 S3에서 이미지 다운로드 후 Twitter에 이미지 업로드
	 */
	public String uploadMedia(String presignedUrl, String accessToken) throws TwitterException {
		byte[] imageBytes = twitterRestClient.downloadImageFromS3(presignedUrl);
		if (imageBytes == null || imageBytes.length == 0) {
			throw new RuntimeException("이미지 다운로드 실패");
		}

		log.info("✅ S3에서 이미지 다운로드 완료 (크기: {} bytes)", imageBytes.length);

		// INIT 요청 (업로드 준비 요청)
		String initResponse = initUpload(imageBytes.length, accessToken);
		String mediaId = extractMediaId(initResponse);

		// APPEND 요청 (이미지 업로드)
		appendMedia(mediaId, imageBytes, accessToken);

		// FINALIZE 요청
		String finalizeResponse = finalizeUpload(mediaId, accessToken);
		return extractMediaId(finalizeResponse);
	}

	/**
	 * INIT 요청 (미디어 업로드 세션 생성)
	 */
	private String initUpload(int totalBytes, String accessToken) throws TwitterException {
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("command", "INIT");
		body.add("total_bytes", String.valueOf(totalBytes));
		body.add("media_type", "image/jpeg");

		return twitterRestClient.postMediaRequest(body, accessToken, TWITTER_MEDIA_UPLOAD_URL);
	}

	/**
	 * APPEND 요청 (이미지 데이터 추가)
	 */
	private void appendMedia(String mediaId, byte[] imageBytes, String accessToken) throws TwitterException {
		MultipartBodyBuilder builder = new MultipartBodyBuilder();
		builder.part("command", "APPEND");
		builder.part("media_id", mediaId);
		builder.part("segment_index", "0");
		builder.part("media", new ByteArrayResource(imageBytes) {
			@Override
			public String getFilename() {
				return "upload.jpg";
			}
		}).contentType(MediaType.IMAGE_JPEG);

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		builder.build().forEach(
			(key, value) -> body.put(key, new ArrayList<>(value))
		);

		twitterRestClient.postMediaRequest(body, accessToken, TWITTER_MEDIA_UPLOAD_URL);
	}

	/**
	 * FINALIZE 요청 (업로드 완료)
	 */
	private String finalizeUpload(String mediaId, String accessToken) throws TwitterException {
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("command", "FINALIZE");
		body.add("media_id", mediaId);

		return twitterRestClient.postMediaRequest(body, accessToken, TWITTER_MEDIA_UPLOAD_URL);
	}

	/**
	 * 응답 JSON에서 media_id 추출
	 */
	private String extractMediaId(String responseBody) {
		try {
			JsonNode jsonNode = objectMapper.readTree(responseBody);
			JsonNode dataNode = jsonNode.get("data");

			if (dataNode != null && dataNode.has("id")) {
				return dataNode.get("id").asText();
			}
			return null;
		} catch (Exception e) {
			log.error("Twitter 응답 JSON 파싱 실패", e);
			throw new RuntimeException("Twitter 응답 JSON 파싱 실패", e);
		}
	}
}
