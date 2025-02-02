package org.mainapplication.domain.post.controller.request;

import java.time.LocalDateTime;
import java.util.List;

import org.domainmodule.post.entity.type.PostStatusType;
import org.mainapplication.domain.post.controller.request.type.UpdatePostType;
import org.springframework.lang.Nullable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "게시물 개별 일반 수정 API 요청 본문")
public class UpdatePostBasicRequest {

	@Schema(description = "게시물 수정 타입에 대한 Enum (상태 수정, 업로드 예약일시 수정, 내용 수정, 내용과 이미지 수정)", example = "STATUS")
	@NotNull(message = "게시물 수정 타입을 지정해주세요.")
	private UpdatePostType updateType;

	@Schema(description = "게시물 상태를 수정하는 경우, 변경할 상태 값", example = "EDITING")
	@Nullable
	private PostStatusType status;

	@Schema(description = "게시물 업로드 예약일시를 수정하는 경우, 변경할 업로드 예약일시", example = "2025-01-01T00:00:00.000Z")
	@Nullable
	private LocalDateTime uploadTime;

	@Schema(description = "게시물 본문 내용을 수정하는 경우, 변경할 본문 내용", example = "이렇게 바꿨다네")
	@Nullable
	private String content;

	@Schema(description = "게시물 이미지를 수정하는 경우, 이미지 URL 리스트")
	@Nullable
	private List<String> imageUrls;
}
