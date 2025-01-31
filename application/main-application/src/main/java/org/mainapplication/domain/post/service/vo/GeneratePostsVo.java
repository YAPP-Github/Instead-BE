package org.mainapplication.domain.post.service.vo;

import java.util.List;

import org.domainmodule.postgroup.entity.PostGroup;
import org.domainmodule.postgroup.entity.PostGroupImage;
import org.domainmodule.postgroup.entity.type.PostGroupPurposeType;
import org.domainmodule.postgroup.entity.type.PostLengthType;
import org.domainmodule.rssfeed.entity.type.FeedCategoryType;
import org.mainapplication.domain.post.controller.request.CreatePostsRequest;

public record GeneratePostsVo(
	String topic,
	PostGroupPurposeType purpose,
	PostLengthType length,
	String content,
	FeedCategoryType newsCategory,
	List<String> imageUrls,
	Integer limit
) {

	public static GeneratePostsVo of(CreatePostsRequest request, Integer limit) {
		return new GeneratePostsVo(
			request.getTopic(),
			request.getPurpose(),
			request.getLength(),
			request.getContent(),
			request.getNewsCategory(),
			request.getImageUrls(),
			limit
		);
	}

	public static GeneratePostsVo of(PostGroup postGroup, Integer limit) {
		List<String> imageUrls = postGroup.getPostGroupImages().stream()
			.map(PostGroupImage::getUrl)
			.toList();

		return new GeneratePostsVo(
			postGroup.getTopic(),
			postGroup.getPurpose(),
			postGroup.getLength(),
			postGroup.getContent(),
			postGroup.getFeed().getCategory(),
			imageUrls,
			limit
		);
	}
}
