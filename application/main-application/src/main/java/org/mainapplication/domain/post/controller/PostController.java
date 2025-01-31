package org.mainapplication.domain.post.controller;

import org.mainapplication.domain.post.controller.request.CreatePostsRequest;
import org.mainapplication.domain.post.controller.response.CreatePostsResponse;
import org.mainapplication.domain.post.exception.PostErrorCode;
import org.mainapplication.domain.post.service.PostService;
import org.mainapplication.global.error.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/agents/{agentId}/post-groups")
@RequiredArgsConstructor
@Tag(name = "Post API", description = "게시물에 대한 요청을 처리하는 API입니다.")
public class PostController {

	private final PostService postService;

	@Operation(summary = "게시물 그룹 및 게시물 생성 API", description = "에이전트에 새 게시물 그룹을 추가하고 게시물을 생성합니다.")
	@PostMapping("/posts")
	public ResponseEntity<CreatePostsResponse> createPosts(
		@PathVariable Long agentId,
		@RequestParam(defaultValue = "5") Integer limit,
		@RequestBody CreatePostsRequest createPostsRequest
	) {
		return switch (createPostsRequest.getReference()) {
			// referenceType이 NONE인 경우
			case NONE -> ResponseEntity.ok(postService.createPostsWithoutRef(createPostsRequest, limit));

			// referenceType이 NEWS인 경우: newsCategory가 null이면 에러 응답
			case NEWS -> {
				if (createPostsRequest.getNewsCategory() == null) {
					throw new CustomException(PostErrorCode.NO_NEWS_CATEGORY);
				}
				yield ResponseEntity.ok(postService.createPostsByNews(createPostsRequest, limit));
			}

			// referenceType이 IMAGE인 경우: imageUrls가 null이면 에러 응답
			case IMAGE -> {
				if (createPostsRequest.getImageUrls() == null) {
					throw new CustomException(PostErrorCode.NO_IMAGE_URLS);
				}
				yield ResponseEntity.ok(postService.createPostsByImage(createPostsRequest, limit));
			}
		};
	}

	@Operation(summary = "게시물 추가 생성 API", description = "기존 게시물 그룹에 새 게시물을 추가합니다.")
	@PostMapping("/{postGroupId}/posts")
	public ResponseEntity<CreatePostsResponse> createAdditionalPosts(
		@PathVariable Long agentId,
		@PathVariable Long postGroupId,
		@RequestParam(defaultValue = "5") Integer limit
	) {
		return ResponseEntity.ok(postService.createAdditionalPosts(postGroupId, limit));
	}
}
