package org.snsclient.client;

import org.snsclient.exception.ImageDownloadException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageDownloadClient {

	private final WebClient webClient;

	/**
	 * Presigned URL을 사용하여 S3에서 이미지 다운로드
	 */
	public byte[] downloadImage(String presignedUrl) {
		try {
			byte[] imageBytes = webClient.get()
				.uri(presignedUrl)
				.retrieve()
				.bodyToMono(byte[].class)
				.block();

			validateImageBytes(imageBytes);

			return imageBytes;
		} catch (Exception e) {
			log.error("S3 이미지 다운로드 중 에러 발생: {}", e.getMessage(), e);
			throw new ImageDownloadException("이미지 다운로드 중 에러 발생", e);
		}
	}

	private void validateImageBytes(byte[] imageBytes) {
		if (imageBytes == null || imageBytes.length == 0) {
			throw new ImageDownloadException("S3 이미지 다운로드 실패");
		}
	}
}
