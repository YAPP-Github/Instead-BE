package org.mainapplication.domain.post.service;

import org.domainmodule.postgroup.entity.type.PostGroupPurposeType;
import org.domainmodule.postgroup.entity.type.PostGroupReferenceType;
import org.domainmodule.postgroup.entity.type.PostLengthType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mainapplication.domain.post.controller.request.CreatePostsRequest;
import org.mainapplication.domain.post.controller.response.CreatePostsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class PostServiceTest {

	@Autowired
	PostService postService;

	@Test
	@Transactional
	void createPosts() {
		// Given
		CreatePostsRequest request = new CreatePostsRequest(
			"오늘의 점심 또는 저녁 메뉴",
			PostGroupPurposeType.OPINION,
			PostGroupReferenceType.NONE,
			null,
			null,
			PostLengthType.SHORT,
			"오늘 이 메뉴는 어떨까나~"
		);

		// When
		CreatePostsResponse response = postService.createPosts(request);
		response.getPosts().forEach(System.out::println);

		// Then
		Assertions.assertAll(
			() -> Assertions.assertEquals(1, response.getPostGroupId()),
			() -> Assertions.assertNull(response.getEof()),
			() -> Assertions.assertEquals(5, response.getPosts().size())
		);
	}
}
