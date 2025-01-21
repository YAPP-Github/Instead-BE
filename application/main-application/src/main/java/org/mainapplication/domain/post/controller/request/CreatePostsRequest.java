package org.mainapplication.domain.post.controller.request;

import org.domainmodule.postgroup.entity.type.PostGroupPurpose;
import org.domainmodule.postgroup.entity.type.PostGroupReference;
import org.domainmodule.postgroup.entity.type.PostLength;
import org.domainmodule.rssfeed.entity.type.FeedCategory;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;

@Getter
public class CreatePostsRequest {

	private String topic;

	private PostGroupPurpose purpose;

	private PostGroupReference reference;

	@Nullable
	private FeedCategory newsCategory;

	@Nullable
	private MultipartFile image;

	private PostLength length;

	private String content;
}
