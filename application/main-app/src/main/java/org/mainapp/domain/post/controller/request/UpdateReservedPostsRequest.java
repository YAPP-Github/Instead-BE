package org.mainapp.domain.post.controller.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "계정별 예약 게시물 예약일시 수정 API 요청 본문")
public record UpdateReservedPostsRequest(
	@Schema(description = "게시물 id", example = "1")
	@NotNull(message = "게시물 id를 지정해주세요.")
	Long postId,
	@Schema(description = "게시물 그룹 id", example = "1")
	@NotNull(message = "게시물 그룹 id를 지정해주세요.")
	Long postGroupId,
	@Schema(description = "변경할 업로드 예약일시", example = "2025-01-01T00:00:00.000Z")
	@NotNull(message = "업로드 예약일시를 지정해주세요.")
	LocalDateTime uploadTime
) {
}
