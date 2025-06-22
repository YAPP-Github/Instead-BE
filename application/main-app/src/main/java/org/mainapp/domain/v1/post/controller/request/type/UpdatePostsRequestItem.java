package org.mainapp.domain.v1.post.controller.request.type;

import java.time.LocalDateTime;

import org.domainmodule.post.entity.type.PostStatusType;
import org.springframework.lang.Nullable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "게시물 기타 정보 수정 API의 수정 정보를 담는 요청 객체")
public class UpdatePostsRequestItem {

	@Schema(description = "수정할 게시물 id", example = "1")
	@NotNull(message = "게시물 id를 지정해주세요.")
	private Long postId;

	@Schema(description = "게시물 상태를 수정하는 경우, 변경할 상태 값", example = "EDITING")
	@Nullable
	private PostStatusType status;

	@Schema(description = "게시물 업로드 예약일시를 수정하는 경우, 변경할 업로드 예약일시", example = "2025-01-01T00:00:00.000Z")
	@Nullable
	private LocalDateTime uploadTime;

	@Schema(description = "게시물 순서를 수정하는 경우, 변경할 게시물 순서", example = "1")
	@Nullable
	private Integer displayOrder;
}
