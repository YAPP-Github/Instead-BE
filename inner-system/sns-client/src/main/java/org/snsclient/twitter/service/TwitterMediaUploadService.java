package org.snsclient.twitter.service;

import java.util.ArrayList;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwitterMediaUploadService {

	private final WebClient webClient;
	private final ObjectMapper objectMapper;

	private final String UPLOAD_URL = "https://api.x.com/2/media/upload";

	/**
	 * Presigned URL을 사용해 S3에서 이미지 다운로드 후 Twitter에 업로드
	 */
	public String uploadMedia(String presignedUrl, String accessToken) {
		byte[] imageBytes = downloadImageFromS3(presignedUrl);
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
	 * WebClient로 S3에서 이미지 다운로드
	 */
	private byte[] downloadImageFromS3(String presignedUrl) {
		return webClient.get()
			.uri(presignedUrl)
			.retrieve()
			.bodyToMono(byte[].class)
			.block();
	}

	/**
	 * INIT 요청 (미디어 업로드 세션 생성)
	 */
	private String initUpload(int totalBytes, String accessToken) {
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("command", "INIT");
		body.add("total_bytes", String.valueOf(totalBytes));
		body.add("media_type", "image/jpeg");

		return sendPostRequest(body, accessToken);
	}

	/**
	 * APPEND 요청 (이미지 데이터 추가)
	 */
	private void appendMedia(String mediaId, byte[] imageBytes, String accessToken) {
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

		sendPostRequest(body, accessToken);
	}

	/**
	 * FINALIZE 요청 (업로드 완료)
	 */
	private String finalizeUpload(String mediaId, String accessToken) {
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("command", "FINALIZE");
		body.add("media_id", mediaId);

		return sendPostRequest(body, accessToken);
	}

	/**
	 * Twitter API에 POST 요청을 보내고 media_id 추출
	 */
	private String sendPostRequest(MultiValueMap<String, Object> body, String accessToken) {
		log.info("📢 Twitter API 요청: {}", body);

		try {
			String response = webClient.post()
				.uri(UPLOAD_URL)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.bodyValue(body)
				.retrieve()
				.bodyToMono(String.class)
				.block();

			log.info("✅ Twitter 응답: {}", response);

			return response;
		} catch (Exception e) {
			log.error("Twitter Media Upload 요청 중 에러 발생", e);
			throw new RuntimeException("Twitter Media Upload 요청 중 에러 발생: " + e.getMessage(), e);
		}
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
