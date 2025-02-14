package org.mainapp.domain.post.service.dto;

import java.util.List;

import org.domainmodule.post.entity.Post;
import org.domainmodule.postgroup.entity.PostGroup;

public record SavePostGroupAndPostsDto(PostGroup postGroup, List<Post> posts) {
}
