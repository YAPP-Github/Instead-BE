package org.mainapp.domain.post.controller.request.type;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "계정별 예약 게시물 예약일시 수정 API의 수정 정보를 담는 요청 객체")
public record UpdateReservedPostsRequestItem(
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
