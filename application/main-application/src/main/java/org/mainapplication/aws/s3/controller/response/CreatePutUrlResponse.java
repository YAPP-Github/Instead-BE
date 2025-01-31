package org.mainapplication.aws.s3.controller.response;

import java.net.URL;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreatePutUrlResponse {

	private URL presignedUrl;

	private Integer duration;

	private String imageUrl;
}
