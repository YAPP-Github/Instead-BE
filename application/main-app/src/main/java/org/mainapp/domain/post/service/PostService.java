package org.mainapp.domain.post.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.domainmodule.post.entity.Post;
import org.domainmodule.post.entity.PostImage;
import org.domainmodule.post.entity.type.PostStatusType;
import org.domainmodule.post.repository.PostImageRepository;
import org.domainmodule.post.repository.PostRepository;
import org.domainmodule.postgroup.entity.PostGroup;
import org.domainmodule.postgroup.repository.PostGroupRepository;
import org.mainapp.domain.post.controller.request.CreatePostsRequest;
import org.mainapp.domain.post.controller.request.MultiplePostUpdateRequest;
import org.mainapp.domain.post.controller.request.SinglePostUpdateRequest;
import org.mainapp.domain.post.controller.request.UpdatePostContentRequest;
import org.mainapp.domain.post.controller.request.UpdatePostsRequest;
import org.mainapp.domain.post.controller.request.type.UpdatePostsRequestItem;
import org.mainapp.domain.post.controller.response.CreatePostsResponse;
import org.mainapp.domain.post.controller.response.GetPostGroupPostsResponse;
import org.mainapp.domain.post.controller.response.type.PostResponse;
import org.mainapp.domain.post.exception.PostErrorCode;
import org.mainapp.global.constants.PostGenerationCount;
import org.mainapp.global.error.CustomException;
import org.mainapp.openai.contentformat.jsonschema.SummaryContentSchema;
import org.mainapp.openai.contentformat.response.SummaryContentFormat;
import org.mainapp.openai.prompt.CreatePostPromptTemplate;
import org.openaiclient.client.OpenAiClient;
import org.openaiclient.client.dto.request.ChatCompletionRequest;
import org.openaiclient.client.dto.response.ChatCompletionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

	// 현재 패키지
	private final PostCreateService postCreateService;
	private final PostTransactionService postTransactionService;
	private final CreatePostPromptTemplate createPostPromptTemplate;
	private final SummaryContentSchema summaryContentSchema;

	// clients 모듈
	private final OpenAiClient openAiClient;

	// domain 모듈
	private final PostRepository postRepository;
	private final PostImageRepository postImageRepository;
	private final PostGroupRepository postGroupRepository;

	// 기타
	private final ObjectMapper objectMapper;

	@Value("${client.openai.model}")
	private String openAiModel;

	/**
	 * 게시물 그룹 및 게시물 생성 메서드.
	 */
	public CreatePostsResponse createPosts(CreatePostsRequest request, Integer limit) {
		return switch (request.getReference()) {
			case NONE -> postCreateService.createPostsWithoutRef(request, limit);
			case NEWS -> postCreateService.createPostsByNews(request, limit);
			case IMAGE -> postCreateService.createPostsByImage(request, limit);
		};
	}

	/**
	 * 게시물 추가 생성 메서드.
	 * postGroupId를 바탕으로 DB에서 PostGroup을 조회한 뒤, referenceType에 맞는 메서드 호출
	 */
	public CreatePostsResponse createAdditionalPosts(Long postGroupId, Integer limit) {
		// PostGroup 조회
		PostGroup postGroup = postGroupRepository.findById(postGroupId)
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
	 * String으로 응답되는 OpenAI API의 응답 content를 SummaryContentFormat 형태로 파싱하는 메서드
	 * 파싱에 실패한 경우 대체 content를 반환
	 */
	private SummaryContentFormat parseSummaryContentFormat(String content) {
		try {
			return objectMapper.readValue(content, SummaryContentFormat.class);
		} catch (JsonProcessingException e) {
			return SummaryContentFormat.createAlternativeFormat("생성된 게시물", content);
		}
	}

	private ChatCompletionResponse applyPrompt(String prompt, String previousResponse) {
		// 프롬프트 생성: Instruction
		String instructionPrompt = createPostPromptTemplate.getInstruction();

		ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest(
			openAiModel, summaryContentSchema.getResponseFormat(), 1, null)
			.addDeveloperMessage(instructionPrompt)
			.addAssistantMessage(previousResponse)
			.addUserTextMessage(prompt);

		try {
			return openAiClient.getChatCompletion(chatCompletionRequest);
		} catch (RuntimeException e) {
			throw new CustomException(PostErrorCode.POST_GENERATE_FAILED);
		}
	}

	/**
	 * 단일 게시물을 prompt를 적용하여 업데이트하는 메서드
	 */
	@Transactional
	public PostResponse updateSinglePostByPrompt(SinglePostUpdateRequest request, Long agentId, Long postGroupId,
		Long postId) {
		Post post = postTransactionService.getPostOrThrow(postId);
		// 프롬프트 적용
		SummaryContentFormat newContent = updatePostContent(post, request.prompt());
		// DB값 업데이트
		return postTransactionService.updateSinglePostAndPromptyHistory(post, request.prompt(), newContent);
	}

	/**
	 * 일괄로 게시물들을 prompt 적용 후 업데이트 하는 메서드
	 */
	public List<PostResponse> updateMultiplePostsByPrompt(MultiplePostUpdateRequest request, Long agentId,
		Long postGroupId) {
		List<Long> postIds = request.postsId();
		String prompt = request.prompt();

		List<Post> posts = postIds.stream()
			.map(postTransactionService::getPostOrThrow)
			.toList();

		List<CompletableFuture<SummaryContentFormat>> futures = posts.stream()
			.map(post -> CompletableFuture.supplyAsync(() -> updatePostContent(post, prompt)))
			.toList();

		// 모든 프롬프트 처리 완료 대기
		List<SummaryContentFormat> newContents = futures.stream()
			.map(CompletableFuture::join)
			.toList();

		// DB 업데이트 및 결과 반환
		return postTransactionService.updateMutiplePostAndPromptyHistory(posts, prompt, newContents);
	}

	private SummaryContentFormat updatePostContent(Post post, String prompt) {
		String previousResponse = post.getContent();

		// ChatGPT 프롬프트 실행
		ChatCompletionResponse result = applyPrompt(prompt, previousResponse);

		// 결과 파싱하여 새로운 요약 + 본문 생성
		return parseSummaryContentFormat(
			result.getChoices().get(0).getMessage().getContent()
		);
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
	 * 게시물 내용 수정 메서드. updateType에 따라 분기
	 */
	public void updatePostContent(Long postGroupId, Long postId, UpdatePostContentRequest request) {
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

		// content 필드 검증 및 Post 엔티티 수정
		if (request.getContent() == null) {
			throw new CustomException(PostErrorCode.INVALID_UPDATING_POST_TYPE);
		}
		postTransactionService.updatePostContent(post, request.getContent());

		// 수정 타입에 따라 분기
		switch (request.getUpdateType()) {
			case CONTENT -> postTransactionService.updatePostContent(post, request.getContent());
			case CONTENT_IMAGE -> updatePostImages(post, request);
		}
	}

	/**
	 * 게시물의 내용 및 이미지 수정 메서드.
	 */
	private void updatePostImages(Post post, UpdatePostContentRequest request) {
		// 수정 타입 검증
		if (request.getContent() == null || request.getImageUrls() == null) {
			throw new CustomException(PostErrorCode.INVALID_UPDATING_POST_TYPE);
		}

		// PostImage 엔티티 조회
		List<PostImage> postImages = postImageRepository.findAllByPost(post);
		List<String> savedImageUrls = postImages.stream()
			.map(PostImage::getUrl)
			.toList();

		// 요청에만 존재하는 PostImage 엔티티 생성
		List<PostImage> newPostImages = request.getImageUrls().stream()
			.filter(imageUrl -> !savedImageUrls.contains(imageUrl))
			.map(newImageUrl -> PostImage.create(post, newImageUrl))
			.toList();
		postTransactionService.savePostImages(newPostImages);

		// DB에만 존재하는 PostImage 엔티티 제거
		List<PostImage> removedPostImages = postImages.stream()
			.filter(postImage -> !request.getImageUrls().contains(postImage.getUrl()))
			.toList();
		postTransactionService.deletePostImages(removedPostImages);
	}

	/**
	 * 게시물 기타 정보 수정 메서드.
	 */
	public void updatePosts(Long postGroupId, UpdatePostsRequest request) {
		// PostGroup 엔티티 조회
		PostGroup postGroup = postGroupRepository.findById(postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));

		// Post 엔티티 리스트 조회
		List<Long> postIds = request.getPosts().stream()
			.map(UpdatePostsRequestItem::getPostId)
			.toList();
		List<Post> posts = postRepository.findAllById(postIds);

		// 검증: PostGroup에 해당하는 Post가 맞는지 검증
		posts.forEach(post -> {
			if (!post.getPostGroup().getId().equals(postGroupId)) {
				throw new CustomException(PostErrorCode.INVALID_POST);
			}
		});

		// Post 엔티티 리스트 수정
		request.getPosts()
			.forEach(postRequest -> {
				Post post = postRepository.findById(postRequest.getPostId())  // 1차 캐시 조회
					.orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

				if (postRequest.getStatus() != null) {
					post.updateStatus(postRequest.getStatus());
				}
				if (postRequest.getUploadTime() != null) {
					// 업로드 예약일시가 변경되는 경우는 업로드 예약 상태가 되는 경우만 존재
					post.updateStatus(PostStatusType.UPLOAD_RESERVED);
					post.updateUploadTime(postRequest.getUploadTime());
				}
				if (postRequest.getDisplayOrder() != null) {
					post.updateDisplayOrder(postRequest.getDisplayOrder());
				}
			});

		// 수정 내용 저장
		postTransactionService.savePosts(posts);
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
