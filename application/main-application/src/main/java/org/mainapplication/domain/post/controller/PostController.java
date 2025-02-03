package org.mainapplication.domain.post.controller;

import java.util.List;

import org.mainapplication.domain.post.controller.request.CreatePostsRequest;
import org.mainapplication.domain.post.controller.request.UpdatePostContentRequest;
import org.mainapplication.domain.post.controller.request.UpdatePostRequest;
import org.mainapplication.domain.post.controller.request.UpdatePostsRequest;
import org.mainapplication.domain.post.controller.response.CreatePostsResponse;
import org.mainapplication.domain.post.controller.response.PromptHistoriesResponse;
import org.mainapplication.domain.post.controller.response.type.PostResponse;
import org.mainapplication.domain.post.service.PostService;
import org.mainapplication.domain.post.service.PromptHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

	@Operation(
		summary = "게시물 그룹 및 게시물 생성 API",
		description = """
			에이전트에 새 게시물 그룹을 추가하고 게시물을 생성합니다.

			**1. 생성 방식을 나타내는 reference 필드 값에 따라 필요한 필드가 달라집니다.**
			- NONE (참고자료 X): newsCategory, imageUrls 필드를 모두 비워주세요.
			- NEWS (뉴스 참고): newsCategory를 지정하고, imageUrls를 비워주세요.
			- IMAGE (이미지 참고): imageUrls를 설정하고, newsCategory를 비워주세요.

			**2. 뉴스를 참고해 생성하는 경우, 응답 본문에 eof가 포함됩니다.**

			한 시점에 사용 가능한 피드 수에 제한이 있기 때문에, 추가 생성이 가능한지 여부를 eof로 구분합니다."""
	)
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

	@Operation(
		summary = "게시물 추가 생성 API",
		description = """
			기존 게시물 그룹에 새 게시물을 추가합니다.

			**뉴스를 참고해 생성하는 게시물 그룹의 경우, 응답 본문에 eof가 포함됩니다.**

			한 시점에 사용 가능한 피드 수에 제한이 있기 때문에, 추가 생성이 가능한지 여부를 eof로 구분합니다."""
	)
	@PostMapping("/{postGroupId}/posts")
	public ResponseEntity<CreatePostsResponse> createAdditionalPosts(
		@PathVariable Long agentId,
		@PathVariable Long postGroupId,
		@RequestParam(defaultValue = "5") Integer limit
	) {
		return ResponseEntity.ok(postService.createAdditionalPosts(postGroupId, limit));
	}

	@Operation(
		summary = "게시물 내용 수정 API",
		description = """
			기존 게시물의 내용 및 이미지를 수정합니다.

			**1. 수정 타입을 나타내는 updateType 필드에 따라 필요한 필드가 달라집니다.**
			- CONTENT (게시물 본문 내용 수정): content 필드를 설정하고, imageUrls 필드는 비워주세요.
			- CONTENT_IMAGE (게시물 본문 내용과 이미지 수정): content 필드와 imageUrls 필드를 설정해주세요. 이미지만 변경하는 경우에도 content 필드를 설정해주어야 합니다.

			**2. 게시물의 이미지 추가나 삭제를 해당 API에서 처리합니다.**

			게시물 이미지에 수정 사항이 있다면, updateType을 CONTENT_IMAGE로 설정하고 수정된 이미지 URL 리스트를 imageUrls 필드에 담아주시면 됩니다.

			이미지 리스트를 보내주시면, 서버에서 DB에 저장된 기존 이미지 리스트를 조회해 두 버전을 비교하고 반영합니다."""
	)
	@PutMapping("/{postGroupId}/posts/{postId}")
	public ResponseEntity<Void> updatePostContent(
		@PathVariable Long agentId,
		@PathVariable Long postGroupId,
		@PathVariable Long postId,
		@RequestBody UpdatePostContentRequest updatePostContentRequest
	) {
		postService.updatePostContent(postGroupId, postId, updatePostContentRequest);
		return ResponseEntity.ok().build();
	}

	@Operation(
		summary = "게시물 기타 정보 수정 API",
		description = """
			기존 여러 게시물들의 상태 / 업로드 예약 일시 / 순서를 수정합니다.

			**변경이 필요한 필드에만 값을 넣어주시고, 변경이 없는 필드는 비워주시면 됩니다.**"""
	)
	@PutMapping("/{postGroupId}/posts")
	public ResponseEntity<Void> updatePosts(
		@PathVariable Long agentId,
		@PathVariable Long postGroupId,
		@RequestBody UpdatePostsRequest updatePostsRequest
	) {
		postService.updatePosts(postGroupId, updatePostsRequest);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "게시물 프롬프트 내역 조회 API", description = "게시물 결과 수정 단계에서 프롬프트 내역을 조회합니다.")
	@GetMapping("/{postGroupId}/posts/{postId}/prompt-histories")
	public ResponseEntity<List<PromptHistoriesResponse>> getPromptHistories(
		@PathVariable Long agentId,
		@PathVariable Long postGroupId,
		@PathVariable Long postId
	) {
		return ResponseEntity.ok(promptHistoryService.getPromptHistories(agentId, postGroupId, postId));
	}

	@Operation(summary = "게시물 그룹별 게시물 목록 조회 API", description = "게시물 그룹에 해당되는 모든 게시물 목록을 조회합니다.")
	@GetMapping("/{postGroupId}/posts")
	public ResponseEntity<List<PostResponse>> getPostsByPostGroup(
		@PathVariable Long agentId,
		@PathVariable Long postGroupId
	) {
		return ResponseEntity.ok(postService.getPostsByPostGroup(postGroupId));
	}

	@Operation(
		summary = "게시물 개별 삭제 API",
		description = """
			업로드가 확정되지 않은 단건의 게시물을 개별 삭제합니다. (생성됨, 수정 중, 수정 완료)

			게시물 수정 단계에서 게시물을 삭제할 때 사용됩니다.

			**업로드가 확정된 상태의 게시물은 삭제할 수 없습니다. (예약 완료, 업로드 완료, 업로드 실패)**"""
	)
	@DeleteMapping("/{postGroupId}/posts/{postId}")
	public ResponseEntity<Void> deletePost(
		@PathVariable Long agentId,
		@PathVariable Long postGroupId,
		@PathVariable Long postId
	) {
		postService.deletePost(postGroupId, postId);
		return ResponseEntity.noContent().build();
	}

	@Operation(
		summary = "게시물 일괄 삭제 API",
		description = """
			업로드가 확정되지 않은 여러 게시물들을 일괄 삭제합니다. (생성됨, 수정 중, 수정 완료)

			게시물 수정 완료 후 예약 단계로 넘어갈 때 사용됩니다.

			**업로드가 확정된 상태의 게시물은 삭제할 수 없습니다. (예약 완료, 업로드 완료, 업로드 실패)**"""
	)
	@DeleteMapping("/{postGroupId}/posts")
	public ResponseEntity<Void> deletePosts(
		@PathVariable Long agentId,
		@PathVariable Long postGroupId,
		@RequestBody List<Long> postIds
	) {
		postService.deletePosts(postGroupId, postIds);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "게시물 프롬프트 기반 개별 수정 API", description = "개별 게시물에 대해 입력된 프롬프트를 바탕으로 수정합니다.")
	@PatchMapping("/{postGroupId}/posts/{postId}/prompt")
	public ResponseEntity<PostResponse> updatePostByPrompt(
		@PathVariable Long agentId,
		@PathVariable Long postGroupId,
		@PathVariable Long postId,
		@RequestBody UpdatePostRequest updatePostRequest
	) {
		return ResponseEntity.ok(postService.updatePostByPrompt(updatePostRequest, agentId, postGroupId, postId));
	}
}
