package org.mainapplication.aws.s3.controller.response;

import java.net.URL;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Pre-signed URL 발급 API 응답 본문")
public class CreatePutUrlResponse {

	@Schema(description = "발급된 pre-signed URL", example = "https://instead-dev.s3.ap-northeast-2.amazonaws.com/~")
	private URL presignedUrl;

	@Schema(description = "pre-signed URL 유지 기간, 분 단위로 표시", example = "5")
	private Integer duration;

	@Schema(description = "발급된 pre-signed URL로 업로드된 이미지에 할당될 이미지 URL", example = "https://instead-dev.s3.ap-northeast-2.amazonaws.com/~")
	private String imageUrl;
}
