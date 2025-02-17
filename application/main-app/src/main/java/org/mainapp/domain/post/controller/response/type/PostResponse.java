package org.mainapp.domain.post.controller.response.type;

import java.time.LocalDateTime;
import java.util.List;

import org.domainmodule.post.entity.Post;
import org.domainmodule.post.entity.type.PostStatusType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "게시물 응답 객체")
public class PostResponse {

	@Schema(description = "게시물 id", example = "1")
	private Long id;

	@Schema(description = "게시물이 포함된 게시물 그룹 id", example = "1")
	private Long postGroupId;

	@Schema(description = "게시물 생성 일시", example = "2025-01-01T00:00:00.000Z")
	private LocalDateTime createdAt;

	@Schema(description = "게시물 마지막 수정 일시", example = "2025-01-01T00:00:00.000Z")
	private LocalDateTime updatedAt;

	@Schema(description = "게시물 정렬 순서 (status 별로 구분)", example = "1")
	private Integer displayOrder;

	@Schema(description = "게시물 한 줄 요약", example = "오늘 점심 메뉴는 두부김치")
	private String summary;

	@Schema(description = "게시물 본문", example = "엄청나게 긴 본문")
	private String content;

	@Schema(description = "게시물에 포함된 이미지 리스트")
	private List<PostImageResponse> postImages;

	@Schema(description = "게시물 상태에 대한 Enum", example = "GENERATED")
	private PostStatusType status;

	@Schema(description = "게시물 업로드 예약 일시", example = "2025-01-01T00:00:00.000Z")
	private LocalDateTime uploadTime;

	public static PostResponse from(Post post) {
		List<PostImageResponse> postImages = post.getPostImages().stream()
			.map(PostImageResponse::from)
			.toList();

		return new PostResponse(
			post.getId(),
			post.getPostGroup().getId(),
			post.getCreatedAt(),
			post.getUpdatedAt(),
			post.getDisplayOrder(),
			post.getSummary(),
			post.getContent(),
			postImages,
			post.getStatus(),
			post.getUploadTime()
		);
	}
}
