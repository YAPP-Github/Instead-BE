package org.mainapplication.domain.post.controller.response.type;

import java.util.List;

import org.domainmodule.postgroup.entity.PostGroup;
import org.domainmodule.postgroup.entity.type.PostGroupLengthType;
import org.domainmodule.postgroup.entity.type.PostGroupPurposeType;
import org.domainmodule.postgroup.entity.type.PostGroupReferenceType;
import org.domainmodule.rssfeed.entity.type.FeedCategoryType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema
public class PostGroupResponse {

	private Long id;

	private String topic;

	private PostGroupPurposeType purpose;

	private PostGroupReferenceType reference;

	private FeedCategoryType newsCategory;

	private List<PostGroupImageResponse> postGroupImages;

	private PostGroupLengthType length;

	private String content;

	public static PostGroupResponse from(PostGroup postGroup) {
		List<PostGroupImageResponse> postGroupImages = postGroup.getPostGroupImages().stream()
			.map(PostGroupImageResponse::from)
			.toList();
		return new PostGroupResponse(
			postGroup.getId(),
			postGroup.getTopic(),
			postGroup.getPurpose(),
			postGroup.getReference(),
			(postGroup.getFeed() != null) ? postGroup.getFeed().getCategory() : null,
			postGroupImages,
			postGroup.getLength(),
			postGroup.getContent()
		);
	}
}
