package org.mainapplication.domain.post.controller.request;

import org.domainmodule.postgroup.entity.type.PostGroupPurposeType;
import org.domainmodule.postgroup.entity.type.PostGroupReferenceType;
import org.domainmodule.postgroup.entity.type.PostLengthType;
import org.domainmodule.rssfeed.entity.type.FeedCategoryType;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreatePostsRequest {

	private String topic;

	private PostGroupPurposeType purpose;

	private PostGroupReferenceType reference;

	@Nullable
	private FeedCategoryType newsCategory;

	@Nullable
	private MultipartFile image;

	private PostLengthType length;

	private String content;
}
