package org.mainapp.domain.post.service.dto;

import java.util.List;

import org.domainmodule.post.entity.Post;
import org.domainmodule.postgroup.entity.PostGroup;
import org.domainmodule.postgroup.entity.PostGroupRssCursor;

public record SavePostGroupWithRssCursorAndPostsDto(
	PostGroup postGroup,
	PostGroupRssCursor postGroupRssCursor,
	List<Post> posts
) {
}
