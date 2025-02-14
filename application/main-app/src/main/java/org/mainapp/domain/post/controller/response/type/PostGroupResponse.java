package org.mainapp.domain.post.controller.response.type;

import java.time.LocalDateTime;
import java.util.List;

import org.domainmodule.postgroup.entity.PostGroup;
import org.domainmodule.postgroup.entity.type.PostGroupLengthType;
import org.domainmodule.postgroup.entity.type.PostGroupPurposeType;
import org.domainmodule.postgroup.entity.type.PostGroupReferenceType;
import org.domainmodule.rssfeed.entity.type.FeedCategoryType;
import org.mainapp.global.constants.PostGenerationCount;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "게시물 그룹 응답 객체")
public class PostGroupResponse {

	@Schema(description = "게시물 그룹 id", example = "1")
	private Long id;

	@Schema(description = "게시물 그룹 생성 일시", example = "2025-01-01T00:00:00.000Z")
	private LocalDateTime createdAt;

	@Schema(description = "게시물 그룹의 주제", example = "점심 메뉴 추천")
	private String topic;

	@Schema(description = "게시물 그룹의 목적에 대한 Enum", example = "OPINION")
	private PostGroupPurposeType purpose;

	@Schema(description = "게시물 생성 방식(참고 자료)에 대한 Enum", example = "NONE")
	private PostGroupReferenceType reference;

	@Schema(description = "뉴스를 참고하는 경우, 뉴스 카테고리", example = "null")
	private FeedCategoryType newsCategory;

	@Schema(description = "이미지를 참고하는 경우, 이미지 객체 리스트", example = "null")
	private List<PostGroupImageResponse> postGroupImages;

	@Schema(description = "게시물 그룹의 길이에 대한 Enum", example = "SHORT")
	private PostGroupLengthType length;

	@Schema(description = "게시물 그룹의 핵심 포함 내용", example = "'이런 메뉴는 어떨까?'와 같은 마무리 멘트")
	private String content;

	@Schema(description = "게시물 그룹의 생성 횟수 제한 도달 여부", example = "false")
	private Boolean eof;

	@Schema(description = "게시물 그룹 썸네일 이미지", example = "https://~")
	private String thumbnailImage;

	public static PostGroupResponse from(PostGroup postGroup) {
		List<PostGroupImageResponse> postGroupImages = postGroup.getPostGroupImages().stream()
			.map(PostGroupImageResponse::from)
			.toList();
		boolean eof = (postGroup.getGenerationCount() >= PostGenerationCount.MAX_POST_GENERATION_COUNT);
		return new PostGroupResponse(
			postGroup.getId(),
			postGroup.getCreatedAt(),
			postGroup.getTopic(),
			postGroup.getPurpose(),
			postGroup.getReference(),
			(postGroup.getFeed() != null) ? postGroup.getFeed().getCategory() : null,
			postGroupImages,
			postGroup.getLength(),
			postGroup.getContent(),
			eof,
			postGroup.getThumbnailImage()
		);
	}
}
