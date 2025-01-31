package org.mainapplication.domain.post.controller.request;

import java.util.List;

import org.domainmodule.postgroup.entity.type.PostGroupPurposeType;
import org.domainmodule.postgroup.entity.type.PostGroupReferenceType;
import org.domainmodule.postgroup.entity.type.PostLengthType;
import org.domainmodule.rssfeed.entity.type.FeedCategoryType;
import org.springframework.lang.Nullable;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreatePostsRequest {

	@NotNull(message = "게시물의 주제를 입력해주세요.")
	private String topic;

	@NotNull(message = "게시물의 목적을 선택해주세요.")
	private PostGroupPurposeType purpose;

	@NotNull(message = "게시물을 생성할 방식을 선택해주세요.")
	private PostGroupReferenceType reference;

	@Nullable
	private FeedCategoryType newsCategory;

	@Nullable
	private List<String> imageUrls;

	@NotNull(message = "게시물의 길이를 선택해주세요.")
	private PostLengthType length;

	@NotNull(message = "게시물의 핵심 내용을 입력해주세요.")
	private String content;
}
