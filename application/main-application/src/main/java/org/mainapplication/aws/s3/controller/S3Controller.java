package org.mainapplication.aws.s3.controller;

import org.mainapplication.aws.s3.controller.response.CreatePutUrlResponse;
import org.mainapplication.aws.s3.service.S3Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/presigned-url")
public class S3Controller {

	private final S3Service s3Service;

	@GetMapping("/post-group")
	public CreatePutUrlResponse getPreSignedPutUrlForPostGroup() {
		return s3Service.createPreSignedPutUrl("post-group/");
	}

	@GetMapping("/post")
	public CreatePutUrlResponse getPreSignedPutUrlForPost() {
		return s3Service.createPreSignedPutUrl("post/");
	}
}
