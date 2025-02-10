package org.mainapp.aws.s3.service;

import java.net.URL;
import java.time.Duration;

import org.mainapp.aws.s3.controller.response.CreatePutUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.uuid.Generators;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class S3Service {

	@Value("${spring.cloud.aws.s3.bucket-name}")
	private String s3BucketName;

	@Value("${spring.cloud.aws.s3.url-prefix}")
	private String s3ImagePrefix;

	private final S3Template s3Template;

	public CreatePutUrlResponse createPreSignedPutUrl(String pathPrefix) {
		String fileName = Generators.timeBasedEpochGenerator().generate().toString();
		URL presignedUrl = s3Template.createSignedPutURL(s3BucketName, pathPrefix + fileName, Duration.ofMinutes(5));
		return new CreatePutUrlResponse(presignedUrl, 5, s3ImagePrefix + pathPrefix + fileName);
	}
}
