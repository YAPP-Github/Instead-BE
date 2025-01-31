package org.mainapplication.domain.post.controller;

import java.util.List;

import org.mainapplication.domain.post.controller.request.CreatePostsRequest;
import org.mainapplication.domain.post.controller.response.CreatePostsResponse;
import org.mainapplication.domain.post.controller.response.PromptHistoriesRespone;
import org.mainapplication.domain.post.service.PostService;
import org.mainapplication.domain.post.service.PromptHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
	private final PromptHistoryService promptHistoryService;

	@Operation(summary = "게시물 그룹 및 게시물 생성 API", description = "에이전트에 새 게시물 그룹을 추가하고 게시물을 생성합니다.")
	@PostMapping("/posts")
	public ResponseEntity<CreatePostsResponse> createPosts(
		@PathVariable Long agentId,
		@RequestParam(defaultValue = "5") Integer limit,
		@Validated @RequestBody CreatePostsRequest createPostsRequest
	) {
		return switch (createPostsRequest.getReference()) {
			case NONE -> ResponseEntity.ok(postService.createPostsWithoutRef(createPostsRequest, limit));
			case NEWS -> ResponseEntity.ok(postService.createPostsByNews(createPostsRequest, limit));
			case IMAGE -> ResponseEntity.ok(postService.createPostsByImage(createPostsRequest, limit));
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

	@Operation(summary = "게시물 프롬프트 내역 조회 API", description = "게시물 결과 수정 단계에서 프롬프트 내역을 조회합니다.")
	@GetMapping("/{postGroupId}/posts/{postId}/prompt-histories")
	public ResponseEntity<List<PromptHistoriesRespone>> getPromptHistories(
		@PathVariable Long agentId,
		@PathVariable Long postGroupId,
		@PathVariable Long postId
	) {
		return ResponseEntity.ok(promptHistoryService.getPromptHistories(agentId, postGroupId, postId));
	}
}
