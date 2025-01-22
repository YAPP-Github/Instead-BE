package org.mainapplication.domain.post.service;

import org.domainmodule.postgroup.entity.type.PostGroupPurposeType;
import org.domainmodule.postgroup.entity.type.PostGroupReferenceType;
import org.domainmodule.postgroup.entity.type.PostLengthType;
import org.domainmodule.rssfeed.entity.type.FeedCategoryType;
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
		CreatePostsResponse response = postService.createPosts(request, 5);

		// Then
		Assertions.assertAll(
			() -> Assertions.assertNotNull(response.getPostGroupId()),
			() -> Assertions.assertNull(response.getEof()),
			() -> Assertions.assertEquals(5, response.getPosts().size())
		);
	}

	@Test
		// @Transactional
	void createPostsByNews() {
		// Given
		CreatePostsRequest request = new CreatePostsRequest(
			"비트코인 소식 알아보기",
			PostGroupPurposeType.INFORMATION,
			PostGroupReferenceType.NEWS,
			FeedCategoryType.COIN,
			null,
			PostLengthType.SHORT,
			"'화성 가즈아~~'와 같은 추임새를 포함하기"
		);

		// When
		CreatePostsResponse response = postService.createPostsByNews(request, 5);

		// Then
		Assertions.assertAll(
			() -> Assertions.assertNotNull(response.getPostGroupId()),
			() -> Assertions.assertFalse(response.getEof()),
			() -> Assertions.assertEquals(5, response.getPosts().size())
		);
	}
}
