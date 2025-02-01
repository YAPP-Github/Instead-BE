package org.mainapplication.domain.post.controller.request;

import java.util.List;

import org.domainmodule.postgroup.entity.type.PostGroupPurposeType;
import org.domainmodule.postgroup.entity.type.PostGroupReferenceType;
import org.domainmodule.postgroup.entity.type.PostLengthType;
import org.domainmodule.rssfeed.entity.type.FeedCategoryType;
import org.springframework.lang.Nullable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "게시물 그룹 및 게시물 생성 API 요청 본문")
public class CreatePostsRequest {

	@Schema(description = "생성할 게시물의 주제", example = "점심 메뉴 추천")
	@NotNull(message = "게시물의 주제를 입력해주세요.")
	private String topic;

	@Schema(description = "생성할 게시물의 목적에 대한 Enum", example = "OPINION")
	@NotNull(message = "게시물의 목적을 선택해주세요.")
	private PostGroupPurposeType purpose;

	@Schema(description = "게시물 생성 방식(참고 자료)에 대한 Enum", example = "NONE")
	@NotNull(message = "게시물을 생성할 방식을 선택해주세요.")
	private PostGroupReferenceType reference;

	@Schema(description = "뉴스 참고하는 경우, 뉴스 카테고리에 대한 Enum", example = "null")
	@Nullable
	private FeedCategoryType newsCategory;

	@Schema(description = "이미지 참고하는 경우, 이미지 URL 리스트", example = "null")
	@Nullable
	private List<String> imageUrls;

	@Schema(description = "생성할 게시물의 길이에 대한 Enum", example = "SHORT")
	@NotNull(message = "게시물의 길이를 선택해주세요.")
	private PostLengthType length;

	@Schema(description = "생성할 게시물에 포함될 핵심 내용", example = "'이런 메뉴는 어떨까?'와 같은 마무리 멘트")
	@NotNull(message = "게시물의 핵심 내용을 입력해주세요.")
	private String content;
}
