package org.mainapplication.aws.s3.controller;

import org.mainapplication.aws.s3.controller.response.CreatePutUrlResponse;
import org.mainapplication.aws.s3.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/presigned-url")
@Tag(name = "Pre-signed URL API", description = "pre-signed URL 발급 관련 API입니다.")
public class S3Controller {

	private final S3Service s3Service;

	@Operation(
		summary = "게시물 그룹 이미지 pre-signed URL 발급 API",
		description = "게시물 그룹에 포함되어 게시물 생성에 사용되는 이미지에 대한 pre-signed URL을 발급합니다.\n\n"
			+ "해당 URL로 업로드하면 서버에서 설정한 파일명으로 업로드되므로, 응답에 포함된 imageUrl 값을 사용하시면 됩니다."
	)
	@GetMapping("/post-group")
	public ResponseEntity<CreatePutUrlResponse> getPreSignedPutUrlForPostGroup() {
		return ResponseEntity.ok(s3Service.createPreSignedPutUrl("post-group/"));
	}

	@Operation(
		summary = "게시물 이미지 pre-signed URL 발급 API",
		description = "게시물에 실제로 포함되는 이미지에 대한 pre-signed URL을 발급합니다.\n\n"
			+ "해당 URL로 업로드하면 서버에서 설정한 파일명으로 업로드되므로, 응답에 포함된 imageUrl 값을 사용하시면 됩니다."
	)
	@GetMapping("/post")
	public ResponseEntity<CreatePutUrlResponse> getPreSignedPutUrlForPost() {
		return ResponseEntity.ok(s3Service.createPreSignedPutUrl("post/"));
	}
}
