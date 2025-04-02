package org.mainapp.domain.v1.post.controller.request;

import java.util.List;

import org.mainapp.domain.v1.post.controller.request.type.UpdatePostContentType;
import org.springframework.lang.Nullable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "게시물 내용 수정 API 요청 본문")
public class UpdatePostContentRequest {

	@Schema(description = "게시물 수정 타입에 대한 Enum (내용 수정, 내용과 이미지 수정)", example = "CONTENT")
	@NotNull(message = "게시물 수정 타입을 지정해주세요.")
	private UpdatePostContentType updateType;

	@Schema(description = "게시물 본문 내용을 수정하는 경우, 변경할 본문 내용", example = "이렇게 바꿨다네")
	@Nullable
	private String content;

	@Schema(description = "게시물 이미지를 수정하는 경우, 이미지 URL 리스트")
	@Nullable
	private List<String> imageUrls;
}
