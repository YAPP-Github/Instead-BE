package org.mainapp.domain.post.service.dto;

import java.util.List;

import org.domainmodule.post.entity.Post;
import org.domainmodule.postgroup.entity.PostGroup;
import org.domainmodule.postgroup.entity.PostGroupImage;

public record SavePostGroupWithImagesAndPostsDto(
	PostGroup postGroup,
	List<PostGroupImage> postGroupImages,
	List<Post> posts
) {
}
