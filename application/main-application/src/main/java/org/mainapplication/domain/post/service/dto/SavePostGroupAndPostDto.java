package org.mainapplication.domain.post.service.dto;

import java.util.List;

import org.domainmodule.post.entity.Post;
import org.domainmodule.postgroup.entity.PostGroup;

public record SavePostGroupAndPostDto(PostGroup postGroup, List<Post> posts) {
}
