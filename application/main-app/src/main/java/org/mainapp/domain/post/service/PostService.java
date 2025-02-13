package org.mainapp.domain.post.service;

import java.util.List;

import org.domainmodule.agent.entity.Agent;
import org.domainmodule.agent.repository.AgentRepository;
import org.domainmodule.post.entity.Post;
import org.domainmodule.post.entity.type.PostStatusType;
import org.domainmodule.post.repository.PostRepository;
import org.domainmodule.postgroup.entity.PostGroup;
import org.domainmodule.postgroup.repository.PostGroupRepository;
import org.mainapp.domain.agent.exception.AgentErrorCode;
import org.mainapp.domain.post.controller.request.CreatePostsRequest;
import org.mainapp.domain.post.controller.request.MultiplePostUpdateRequest;
import org.mainapp.domain.post.controller.request.SinglePostUpdateRequest;
import org.mainapp.domain.post.controller.request.UpdatePostContentRequest;
import org.mainapp.domain.post.controller.request.UpdatePostsMetadataRequest;
import org.mainapp.domain.post.controller.response.CreatePostsResponse;
import org.mainapp.domain.post.controller.response.GetPostGroupPostsResponse;
import org.mainapp.domain.post.controller.response.type.PostResponse;
import org.mainapp.domain.post.exception.PostErrorCode;
import org.mainapp.global.constants.PostGenerationCount;
import org.mainapp.global.error.CustomException;
import org.mainapp.global.util.SecurityUtil;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

	private final PostCreateService postCreateService;
	private final PostUpdateService postUpdateService;
	private final PostPromptUpdateService postPromptUpdateService;
	private final PostTransactionService postTransactionService;
	private final AgentRepository agentRepository;
	private final PostGroupRepository postGroupRepository;
	private final PostRepository postRepository;

	/**
	 * 게시물 그룹 및 게시물 생성 메서드.
	 */
	public CreatePostsResponse createPosts(Long agentId, CreatePostsRequest request, Integer limit) {
		// 사용자 인증 정보 및 Agent 조회
		Long userId = SecurityUtil.getCurrentUserId();
		Agent agent = agentRepository.findByUserIdAndId(userId, agentId)
			.orElseThrow(() -> new CustomException(AgentErrorCode.AGENT_NOT_FOUND));

		return switch (request.getReference()) {
			case NONE -> postCreateService.createPostsWithoutRef(agent, request, limit);
			case NEWS -> postCreateService.createPostsByNews(agent, request, limit);
			case IMAGE -> postCreateService.createPostsByImage(agent, request, limit);
		};
	}

	/**
	 * 게시물 추가 생성 메서드.
	 * postGroupId를 바탕으로 DB에서 PostGroup을 조회한 뒤, referenceType에 맞는 메서드 호출
	 */
	public CreatePostsResponse createAdditionalPosts(Long agentId, Long postGroupId, Integer limit) {
		// 사용자 인증 정보 및 PostGroup 조회
		Long userId = SecurityUtil.getCurrentUserId();
		PostGroup postGroup = postGroupRepository.findByUserIdAndAgentIdAndId(userId, agentId, postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));

		// PostGroup의 게시물 생성 횟수 검증
		if (postGroup.getGenerationCount() >= PostGenerationCount.MAX_POST_GENERATION_COUNT) {
			throw new CustomException(PostErrorCode.EXHAUSTED_GENERATION_COUNT);
		}

		// displayOrder 설정을 위해 Post 조회
		Integer order = postRepository.findLastGeneratedPost(postGroup, PostStatusType.GENERATED)
			.map(Post::getDisplayOrder)
			.orElse(0);

		// referenceType에 따라 분기
		return switch (postGroup.getReference()) {
			case NONE -> postCreateService.createAdditionalPostsWithoutRef(postGroup, limit, order);
			case NEWS -> postCreateService.createAdditionalPostsByNews(postGroup, limit, order);
			case IMAGE -> postCreateService.createAdditionalPostsByImage(postGroup, limit, order);
		};
	}

	/**
	 * postGroupId를 바탕으로 게시물 그룹 존재 여부를 확인하고, 해당 그룹의 게시물 목록을 반환하는 메서드
	 * 게시물 그룹 조회 실패 시 POST_GROUP_NOT_FOUND
	 */
	public GetPostGroupPostsResponse getPostsByPostGroup(Long postGroupId) {
		// PostGroup 엔티티 조회
		PostGroup postGroup = postGroupRepository.findById(postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));

		// Post 엔티티 리스트 조회
		List<Post> posts = postRepository.findAllByPostGroup(postGroup);

		// 결과 반환
		return GetPostGroupPostsResponse.of(postGroup, posts);
	}

	/**
	 * 단일 게시물을 prompt를 적용하여 업데이트하는 메서드
	 */
	public PostResponse updateSinglePostByPrompt(
		SinglePostUpdateRequest request, Long agentId, Long postGroupId, Long postId) {
		return postPromptUpdateService.updateSinglePostByPrompt(request, agentId, postGroupId, postId);
	}

	/**
	 * 일괄로 게시물들을 prompt 적용 후 업데이트 하는 메서드
	 */
	public List<PostResponse> updateMultiplePostsByPrompt(
		MultiplePostUpdateRequest request, Long agentId, Long postGroupId) {
		return postPromptUpdateService.updateMultiplePostsByPrompt(request, agentId, postGroupId);
	}

	/**
	 * 게시물 내용 수정 메서드.
	 */
	public void updatePostContent(Long postGroupId, Long postId, UpdatePostContentRequest request) {
		postUpdateService.updatePostContent(postGroupId, postId, request);
	}

	/**
	 * 게시물 기타 정보 수정 메서드.
	 */
	public void updatePostsMetadata(Long postGroupId, UpdatePostsMetadataRequest request) {
		postUpdateService.updatePostsMetadata(postGroupId, request);
	}

	/**
	 * 업로드가 확정되지 않은 상태의 게시물을 단건 삭제하는 메서드
	 */
	public void deletePost(Long postGroupId, Long postId) {
		// PostGroup 엔티티 조회
		PostGroup postGroup = postGroupRepository.findById(postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));

		// Post 엔티티 조회
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

		// 검증: PostGroup에 해당하는 Post가 맞는지 검증
		if (!post.getPostGroup().getId().equals(postGroupId)) {
			throw new CustomException(PostErrorCode.INVALID_POST);
		}

		// 검증: 삭제하려는 Post의 상태 확인
		validateDeletingPostStatus(post);

		// Post 엔티티 삭제
		postTransactionService.deletePost(post);
	}

	/**
	 * 업로드가 확정되지 않은 상태의 게시물들을 일괄 삭제하는 메서드
	 */
	public void deletePosts(Long postGroupId, List<Long> postIds) {
		// PostGroup 엔티티 조회
		PostGroup postGroup = postGroupRepository.findById(postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));

		// Post 엔티티 리스트 조회
		// TODO: 지금은 단순히 조회한 Post의 개수를 가지고 검증하고 있는데, 상세 postId를 반환하기 위해 로직을 수정할 필요가 있을듯. 지금은 에러메시지에 변수를 담을 수 없는 상황이라 단순한 로직으로 구현함
		List<Post> posts = postRepository.findAllById(postIds);
		if (posts.size() < postIds.size()) {
			throw new CustomException(PostErrorCode.POST_NOT_FOUND);
		}

		// 검증: PostGroup에 해당하는 Post가 맞는지 검증 & 삭제하려는 Post의 상태 확인
		posts.forEach(post -> {
			if (!post.getPostGroup().getId().equals(postGroupId)) {
				throw new CustomException(PostErrorCode.INVALID_POST);
			}
			validateDeletingPostStatus(post);
		});

		// Post 엔티티 리스트 삭제
		postTransactionService.deletePosts(posts);
	}

	/**
	 * 삭제하려는 Post가 업로드 확정되지 않은 상태인지 확인하는 메서드
	 */
	private void validateDeletingPostStatus(Post post) {
		// 삭제 가능한 상태: 생성됨, 수정중, 수정완료
		List<PostStatusType> validStatuses = List.of(PostStatusType.GENERATED, PostStatusType.EDITING,
			PostStatusType.READY_TO_UPLOAD);

		if (!validStatuses.contains(post.getStatus())) {
			throw new CustomException(PostErrorCode.INVALID_DELETING_POST_STATUS);
		}
	}
}
