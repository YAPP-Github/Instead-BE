package org.mainapp.domain.post.controller;

import java.util.List;

import org.mainapp.domain.post.controller.request.CreatePostsRequest;
import org.mainapp.domain.post.controller.request.MultiplePostUpdateRequest;
import org.mainapp.domain.post.controller.request.SinglePostUpdateRequest;
import org.mainapp.domain.post.controller.request.UpdatePostContentRequest;
import org.mainapp.domain.post.controller.request.UpdatePostsMetadataRequest;
import org.mainapp.domain.post.controller.response.CreatePostsResponse;
import org.mainapp.domain.post.controller.response.GetPostGroupPostsResponse;
import org.mainapp.domain.post.controller.response.PromptHistoriesResponse;
import org.mainapp.domain.post.controller.response.type.PostResponse;
import org.mainapp.domain.post.service.PostPromptHistoryService;
import org.mainapp.domain.post.service.PostService;
import org.mainapp.global.constants.PostGenerationCount;
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
	private final PostPromptHistoryService postPromptHistoryService;

	@Operation(
		summary = "게시물 그룹 및 게시물 생성 API",
		description = """
			에이전트에 새 게시물 그룹을 추가하고 게시물을 생성합니다.

			**1. 생성 방식을 나타내는 reference 필드 값에 따라 필요한 필드가 달라집니다.**
			- NONE (참고자료 X): newsCategory, imageUrls 필드를 모두 비워주세요.
			- NEWS (뉴스 참고): newsCategory를 지정하고, imageUrls를 비워주세요.
			- IMAGE (이미지 참고): imageUrls를 설정하고, newsCategory를 비워주세요.

			**2. 응답 본문에 eof가 포함됩니다.**

			게시물 그룹별 최대 게시물 생성 가능 횟수를 채우게 되면 eof가 true로 응답됩니다. 이 경우 추가 생성이 제한됩니다."""
	)
	@PostMapping("/posts")
	public ResponseEntity<CreatePostsResponse> createPosts(
		@PathVariable Long agentId,
		@RequestParam(defaultValue = PostGenerationCount.POST_GENERATION_POST_COUNT) Integer limit,
		@Validated @RequestBody CreatePostsRequest createPostsRequest
	) {
		return ResponseEntity.ok(postService.createPosts(agentId, createPostsRequest, limit));
	}

	@Operation(
		summary = "게시물 추가 생성 API",
		description = """
			기존 게시물 그룹에 새 게시물을 추가합니다.

			**응답 본문에 eof가 포함됩니다.**

			게시물 그룹별 최대 게시물 생성 가능 횟수를 채우게 되면 eof가 true로 응답됩니다. 이 경우 추가 생성이 제한됩니다."""
	)
	@PostMapping("/{postGroupId}/posts")
	public ResponseEntity<CreatePostsResponse> createAdditionalPosts(
		@PathVariable Long agentId,
		@PathVariable Long postGroupId,
		@RequestParam(defaultValue = PostGenerationCount.POST_GENERATION_POST_COUNT) Integer limit
	) {
		return ResponseEntity.ok(postService.createAdditionalPosts(agentId, postGroupId, limit));
	}

	// @Operation(summary = "계정별 게시물 그룹 목록 조회 API", description = "사용자가 연동한 SNS 계정 내의 게시물 그룹 목록을 조회합니다.")

	@Operation(summary = "게시물 그룹별 게시물 목록 조회 API", description = "게시물 그룹에 해당되는 모든 게시물 목록을 조회합니다.")
	@GetMapping("/{postGroupId}/posts")
	public ResponseEntity<GetPostGroupPostsResponse> getPostsByPostGroup(
		@PathVariable Long agentId,
		@PathVariable Long postGroupId
	) {
		return ResponseEntity.ok(postService.getPostsByPostGroup(agentId, postGroupId));
	}

	@Operation(summary = "게시물 프롬프트 내역 조회 API", description = "게시물 결과 수정 단계에서 프롬프트 내역을 조회합니다.")
	@GetMapping("/{postGroupId}/posts/{postId}/prompt-histories")
	public ResponseEntity<List<PromptHistoriesResponse>> getPromptHistories(
		@PathVariable Long agentId,
		@PathVariable Long postGroupId,
		@PathVariable Long postId
	) {
		return ResponseEntity.ok(postPromptHistoryService.getPromptHistories(agentId, postGroupId, postId));
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
		@Validated @RequestBody UpdatePostContentRequest updatePostContentRequest
	) {
		postService.updatePostContent(agentId, postGroupId, postId, updatePostContentRequest);
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
		@Validated @RequestBody UpdatePostsMetadataRequest updatePostsMetadataRequest
	) {
		postService.updatePostsMetadata(agentId, postGroupId, updatePostsMetadataRequest);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "게시물 프롬프트 기반 개별 수정 API", description = "개별 게시물에 대해 입력된 프롬프트를 바탕으로 수정합니다.")
	@PatchMapping("/{postGroupId}/posts/{postId}/prompt")
	public ResponseEntity<PostResponse> updateSinglePostByPrompt(
		@PathVariable Long agentId,
		@PathVariable Long postGroupId,
		@PathVariable Long postId,
		@Validated @RequestBody SinglePostUpdateRequest singlePostUpdateRequest
	) {
		return ResponseEntity.ok(
			postService.updateSinglePostByPrompt(singlePostUpdateRequest, agentId, postGroupId, postId));
	}

	@Operation(summary = "게시물 프롬프트 기반 일괄 수정 API", description = "일괄 게시물에 대해 입력된 프롬프트를 바탕으로 수정합니다.")
	@PatchMapping("/{postGroupId}/posts/prompt")
	public ResponseEntity<List<PostResponse>> updateMultiplePostsByPrompt(
		@PathVariable Long agentId,
		@PathVariable Long postGroupId,
		@Validated @RequestBody MultiplePostUpdateRequest multiplePostUpdateRequest
	) {
		return ResponseEntity.ok(
			postService.updateMultiplePostsByPrompt(multiplePostUpdateRequest, agentId, postGroupId));
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
		postService.deletePost(agentId, postGroupId, postId);
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
		@Validated @RequestBody List<Long> postIds
	) {
		postService.deletePosts(agentId, postGroupId, postIds);
		return ResponseEntity.noContent().build();
	}
}
