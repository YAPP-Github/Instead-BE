package org.mainapplication.domain.post.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.domainmodule.post.entity.Post;
import org.domainmodule.post.entity.PostImage;
import org.domainmodule.post.entity.type.PostStatusType;
import org.domainmodule.post.repository.PostImageRepository;
import org.domainmodule.post.repository.PostRepository;
import org.domainmodule.post.repository.PromptHistoryRepository;
import org.domainmodule.postgroup.entity.PostGroup;
import org.domainmodule.postgroup.entity.PostGroupImage;
import org.domainmodule.postgroup.entity.PostGroupRssCursor;
import org.domainmodule.postgroup.repository.PostGroupRepository;
import org.domainmodule.postgroup.repository.PostGroupRssCursorRepository;
import org.domainmodule.rssfeed.entity.RssFeed;
import org.domainmodule.rssfeed.repository.RssFeedRepository;
import org.feedclient.service.FeedService;
import org.feedclient.service.dto.FeedPagingResult;
import org.mainapplication.domain.post.controller.request.CreatePostsRequest;
import org.mainapplication.domain.post.controller.request.MultiplePostUpdateRequest;
import org.mainapplication.domain.post.controller.request.UpdatePostBasicRequest;
import org.mainapplication.domain.post.controller.request.SinglePostUpdateRequest;
import org.mainapplication.domain.post.controller.request.UpdatePostsBasicRequest;
import org.mainapplication.domain.post.controller.request.type.UpdatePostsBasicRequestItem;
import org.mainapplication.domain.post.controller.response.CreatePostsResponse;
import org.mainapplication.domain.post.controller.response.type.PostResponse;
import org.mainapplication.domain.post.exception.PostErrorCode;
import org.mainapplication.domain.post.service.dto.SavePostGroupAndPostsDto;
import org.mainapplication.domain.post.service.dto.SavePostGroupWithImagesAndPostsDto;
import org.mainapplication.domain.post.service.dto.SavePostGroupWithRssCursorAndPostsDto;
import org.mainapplication.domain.post.service.vo.GeneratePostsVo;
import org.mainapplication.global.error.CustomException;
import org.mainapplication.openai.contentformat.jsonschema.SummaryContentSchema;
import org.mainapplication.openai.contentformat.response.SummaryContentFormat;
import org.mainapplication.openai.prompt.CreatePostPrompt;
import org.openaiclient.client.OpenAiClient;
import org.openaiclient.client.dto.request.ChatCompletionRequest;
import org.openaiclient.client.dto.response.ChatCompletionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

	private final FeedService feedService;
	private final OpenAiClient openAiClient;
	private final PostTransactionService postTransactionService;
	private final RssFeedRepository rssFeedRepository;
	private final CreatePostPrompt createPostPrompt;
	private final SummaryContentSchema summaryContentSchema;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final PostRepository postRepository;
	private final PostGroupRepository postGroupRepository;
	private final PostImageRepository postImageRepository;
	private final PostGroupRssCursorRepository postGroupRssCursorRepository;
	private final PromptHistoryRepository promptHistoryRepository;

	@Value("${client.openai.model}")
	private String openAiModel;

	/**
	 * 참고자료 없는 게시물 생성 및 저장 메서드
	 */
	public CreatePostsResponse createPostsWithoutRef(CreatePostsRequest request, Integer limit) {
		// 게시물 생성
		ChatCompletionResponse result = generatePostsWithoutRef(GeneratePostsVo.of(request, limit));

		// PostGroup 엔티티 생성
		PostGroup postGroup = PostGroup.createPostGroup(null, null, request.getTopic(), request.getPurpose(),
			request.getReference(), request.getLength(), request.getContent());

		// Post 엔티티 생성: OpenAI API 응답의 choices에서 답변 꺼내 json으로 파싱 후 엔티티 생성
		List<Post> posts = result.getChoices().stream()
			.map(choice -> {
				SummaryContentFormat content = parseSummaryContentFormat(choice.getMessage().getContent());
				return Post.create(postGroup, null, content.getSummary(), content.getContent(),
					PostStatusType.GENERATED, null);
			})
			.toList();

		// PostGroup 및 Post 리스트 저장
		SavePostGroupAndPostsDto saveResult = postTransactionService.savePostGroupAndPosts(postGroup, posts);

		// 결과 반환
		List<PostResponse> postResponses = saveResult.posts().stream()
			.map(PostResponse::from)
			.toList();
		return new CreatePostsResponse(saveResult.postGroup().getId(), null, postResponses);
	}

	/**
	 * 뉴스 기사 기반 게시물 생성 및 저장 메서드
	 */
	public CreatePostsResponse createPostsByNews(CreatePostsRequest request, Integer limit) {
		// newsCategory 필드 검증
		if (request.getNewsCategory() == null) {
			throw new CustomException(PostErrorCode.NO_NEWS_CATEGORY);
		}

		// 피드 받아오기
		RssFeed rssFeed = rssFeedRepository.findByCategory(request.getNewsCategory())
			.orElseThrow(() -> new CustomException(PostErrorCode.RSS_FEED_NOT_FOUND));
		FeedPagingResult feedPagingResult = getPagedNews(rssFeed, null, limit);

		// 게시물 생성
		List<ChatCompletionResponse> results = generatePostsByNews(
			GeneratePostsVo.of(request, limit), feedPagingResult);

		// PostGroup 엔티티 생성
		PostGroup postGroup = PostGroup.createPostGroup(null, rssFeed, request.getTopic(), request.getPurpose(),
			request.getReference(), request.getLength(), request.getContent());

		// PostGroupRssCursor 엔티티 생성
		String cursor = feedPagingResult.getFeedItems().get(feedPagingResult.getFeedItems().size() - 1).getId();
		PostGroupRssCursor postGroupRssCursor = PostGroupRssCursor.createPostGroupRssCursor(postGroup, cursor);

		// Post 엔티티 생성
		List<Post> posts = results.stream()
			.map(result -> {
				SummaryContentFormat content = parseSummaryContentFormat(
					result.getChoices().get(0).getMessage().getContent());
				return Post.create(postGroup, null, content.getSummary(), content.getContent(),
					PostStatusType.GENERATED, null);
			})
			.toList();

		// 엔티티 저장
		SavePostGroupWithRssCursorAndPostsDto saveResult = postTransactionService.savePostGroupWithRssCursorAndPosts(
			postGroup, postGroupRssCursor, posts);

		// 결과 반환하기
		List<PostResponse> postResponses = saveResult.posts().stream()
			.map(PostResponse::from)
			.toList();
		return new CreatePostsResponse(saveResult.postGroup().getId(), feedPagingResult.isEof(), postResponses);
	}

	/**
	 * 이미지 기반 게시물 생성 및 저장 메서드
	 */
	public CreatePostsResponse createPostsByImage(CreatePostsRequest request, Integer limit) {
		// imageUrls 필드 검증
		if (request.getImageUrls() == null) {
			throw new CustomException(PostErrorCode.NO_IMAGE_URLS);
		}

		// 게시물 생성
		ChatCompletionResponse result = generatePostsByImage(GeneratePostsVo.of(request, limit));

		// PostGroup 엔티티 생성
		PostGroup postGroup = PostGroup.createPostGroup(null, null, request.getTopic(), request.getPurpose(),
			request.getReference(), request.getLength(), request.getContent());

		// PostGroupImage 엔티티 리스트 생성
		List<PostGroupImage> postGroupImages = request.getImageUrls().stream()
			.map(imageUrl -> PostGroupImage.createPostGroupImage(postGroup, imageUrl))
			.toList();

		// Post 엔티티 리스트 생성
		List<Post> posts = result.getChoices().stream()
			.map(choice -> {
				SummaryContentFormat content = parseSummaryContentFormat(choice.getMessage().getContent());
				return Post.create(postGroup, null, content.getSummary(), content.getContent(),
					PostStatusType.GENERATED, null);
			})
			.toList();

		// 엔티티 저장
		SavePostGroupWithImagesAndPostsDto saveResult = postTransactionService.savePostGroupWithImagesAndPosts(
			postGroup, postGroupImages, posts);

		// 결과 반환
		List<PostResponse> postResponses = saveResult.posts().stream()
			.map(PostResponse::from)
			.toList();
		return new CreatePostsResponse(saveResult.postGroup().getId(), null, postResponses);
	}

	/**
	 * 게시물 추가 생성 메서드.
	 * postGroupId를 바탕으로 DB에서 PostGroup을 조회한 뒤, referenceType에 맞는 메서드 호출
	 */
	public CreatePostsResponse createAdditionalPosts(Long postGroupId, Integer limit) {
		// PostGroup 조회
		PostGroup postGroup = postGroupRepository.findById(postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));

		// referenceType에 따라 분기
		return switch (postGroup.getReference()) {
			case NONE -> createAdditionalPostsWithoutRef(postGroup, limit);
			case NEWS -> createAdditionalPostsByNews(postGroup, limit);
			case IMAGE -> createAdditionalPostsByImage(postGroup, limit);
		};
	}

	/**
	 * 게시물 추가 생성: 참고자료 없는 게시물 생성 및 저장 메서드
	 */
	private CreatePostsResponse createAdditionalPostsWithoutRef(PostGroup postGroup, Integer limit) {
		// 게시물 생성
		ChatCompletionResponse result = generatePostsWithoutRef(GeneratePostsVo.of(postGroup, limit));

		// Post 엔티티 리스트 생성
		List<Post> posts = result.getChoices().stream()
			.map(choice -> {
				SummaryContentFormat content = parseSummaryContentFormat(choice.getMessage().getContent());
				return Post.create(postGroup, null, content.getSummary(), content.getContent(),
					PostStatusType.GENERATED, null);
			})
			.toList();

		// 엔티티 저장
		List<Post> savedPosts = postTransactionService.savePosts(posts);

		// 결과 반환
		List<PostResponse> postResponses = savedPosts.stream()
			.map(PostResponse::from)
			.toList();
		return new CreatePostsResponse(postGroup.getId(), null, postResponses);
	}

	/**
	 * 게시물 추가 생성: 뉴스 기사 기반 게시물 생성 및 저장 메서드
	 * 피드 고갈 시 NEWS_FEED_EXHAUSTED
	 */
	private CreatePostsResponse createAdditionalPostsByNews(PostGroup postGroup, Integer limit) {
		// 피드 받아오기: RssFeed와 PostGroupRssCursor를 DB에서 조회
		RssFeed rssFeed = rssFeedRepository.findByCategory(postGroup.getFeed().getCategory())
			.orElseThrow(() -> new CustomException(PostErrorCode.RSS_FEED_NOT_FOUND));
		PostGroupRssCursor rssCursor = postGroupRssCursorRepository.findByPostGroup(postGroup)
			.orElseThrow(() -> new CustomException(PostErrorCode.RSS_CURSOR_NOT_FOUND));
		FeedPagingResult feedPagingResult = getPagedNews(rssFeed, rssCursor.getNewsId(), limit);

		// 피드가 고갈된 경우 에러 응답
		if (feedPagingResult.getFeedItems().isEmpty()) {
			throw new CustomException(PostErrorCode.NEWS_FEED_EXHAUSTED);
		}

		// 게시물 생성
		List<ChatCompletionResponse> results = generatePostsByNews(
			GeneratePostsVo.of(postGroup, limit), feedPagingResult);

		// PostGroupRssCursor 업데이트
		String cursor = feedPagingResult.getFeedItems().get(feedPagingResult.getFeedItems().size() - 1).getId();
		rssCursor.updateNewsId(cursor);

		// Post 엔티티 생성
		List<Post> posts = results.stream()
			.map(result -> {
				SummaryContentFormat content = parseSummaryContentFormat(
					result.getChoices().get(0).getMessage().getContent());
				return Post.create(postGroup, null, content.getSummary(), content.getContent(),
					PostStatusType.GENERATED, null);
			})
			.toList();

		// 엔티티 저장
		List<Post> savedPosts = postTransactionService.savePosts(posts);

		// 결과 반환하기
		List<PostResponse> postResponses = savedPosts.stream()
			.map(PostResponse::from)
			.toList();
		return new CreatePostsResponse(postGroup.getId(), feedPagingResult.isEof(), postResponses);
	}

	/**
	 * 게시물 추가 생성: 이미지 기반 게시물 생성 및 저장 메서드
	 */
	private CreatePostsResponse createAdditionalPostsByImage(PostGroup postGroup, Integer limit) {
		// 게시물 생성
		ChatCompletionResponse result = generatePostsByImage(GeneratePostsVo.of(postGroup, limit));

		// Post 엔티티 리스트 생성
		List<Post> posts = result.getChoices().stream()
			.map(choice -> {
				SummaryContentFormat content = parseSummaryContentFormat(choice.getMessage().getContent());
				return Post.create(postGroup, null, content.getSummary(), content.getContent(),
					PostStatusType.GENERATED, null);
			})
			.toList();

		// 엔티티 저장
		List<Post> savedPosts = postTransactionService.savePosts(posts);

		// 결과 반환
		List<PostResponse> postResponses = savedPosts.stream()
			.map(PostResponse::from)
			.toList();
		return new CreatePostsResponse(postGroup.getId(), null, postResponses);
	}

	/**
	 * 참고자료 없는 게시물 생성 메서드. (프롬프트 설정 + 게시물 생성 작업 수행)
	 * 예외 발생 시 PostGenerateFailedException 발생
	 */
	private ChatCompletionResponse generatePostsWithoutRef(GeneratePostsVo vo) {
		// 프롬프트 생성: Instruction + 주제 Prompt
		String instructionPrompt = createPostPrompt.getInstruction();
		String topicPrompt = createPostPrompt.getBasicTopicPrompt(vo.topic(), vo.purpose(), vo.length(), vo.content());

		// 게시물 생성
		ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest(
			openAiModel, summaryContentSchema.getResponseFormat(), vo.limit(), null)
			.addDeveloperMessage(instructionPrompt)
			.addUserTextMessage(topicPrompt);

		try {
			return openAiClient.getChatCompletion(chatCompletionRequest);
		} catch (RuntimeException e) {
			throw new CustomException(PostErrorCode.POST_GENERATE_FAILED);
		}
	}

	/**
	 * 뉴스 기사 기반 게시물 생성 메서드. (프롬프트 설정 + 게시물 생성 작업 수행)
	 * 예외 발생 시 PostGenerateFailedException 발생
	 */
	private List<ChatCompletionResponse> generatePostsByNews(GeneratePostsVo vo, FeedPagingResult feedPagingResult) {
		// 프롬프트 생성
		String instructionPrompt = createPostPrompt.getInstruction();
		String topicPrompt = createPostPrompt.getBasicTopicPrompt(
			vo.topic(), vo.purpose(), vo.length(), vo.content());
		List<String> refPrompts = feedPagingResult.getFeedItems().stream()
			.map(news -> createPostPrompt.getNewsRefPrompt(news.getContentSummary(), news.getContent()))
			.toList();

		// 게시물 생성하기: 각 뉴스 기사별로 OpenAI API 호출 및 답변 생성
		List<CompletableFuture<ChatCompletionResponse>> resultFutures = refPrompts.stream()
			.map(refPrompt -> openAiClient.getChatCompletionAsync(
				new ChatCompletionRequest(openAiModel, summaryContentSchema.getResponseFormat(), null, null)
					.addDeveloperMessage(instructionPrompt)
					.addUserTextMessage(topicPrompt)
					.addUserTextMessage(refPrompt)
			))
			.toList();

		// TODO: 해당 client에서 RuntimeException이 아닌 구분되는 예외를 던지도록 수정하기
		try {
			return resultFutures.stream()
				.map(CompletableFuture::join)
				.toList();
		} catch (RuntimeException e) {
			throw new CustomException(PostErrorCode.POST_GENERATE_FAILED);
		}
	}

	/**
	 * 이미지 기반 게시물 생성 메서드. (프롬프트 설정 + 게시물 생성 작업 수행)
	 * 예외 발생 시 PostGenerateFailedException 발생
	 */
	private ChatCompletionResponse generatePostsByImage(GeneratePostsVo vo) {
		// 프롬프트 생성
		String instructionPrompt = createPostPrompt.getInstruction();
		String topicPrompt = createPostPrompt.getBasicTopicPrompt(
			vo.topic(), vo.purpose(), vo.length(), vo.content());
		String imageRefPrompt = createPostPrompt.getImageRefPrompt();

		// 게시물 생성
		ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest(openAiModel,
			summaryContentSchema.getResponseFormat(), vo.limit(), null)
			.addDeveloperMessage(instructionPrompt)
			.addUserTextMessage(topicPrompt)
			.addUserImageMessage(imageRefPrompt, vo.imageUrls());

		// TODO: 해당 client에서 RuntimeException이 아닌 구분되는 예외를 던지도록 수정하기
		try {
			return openAiClient.getChatCompletion(chatCompletionRequest);
		} catch (RuntimeException e) {
			throw new CustomException(PostErrorCode.POST_GENERATE_FAILED);
		}
	}

	/**
	 * 요청한 뉴스 카테고리에 따라 뉴스 피드를 가져오는 메서드
	 * 피드 가져오기 실패 시 NEWS_GET_FAILED
	 */
	// TODO: 메서드 분리 없애기. 해당 client에서 RuntimeException이 아닌 구분되는 예외를 던지도록 수정하기
	private FeedPagingResult getPagedNews(RssFeed rssFeed, String cursor, Integer limit) {
		try {
			if (cursor == null) {
				return feedService.getPagedFeed(rssFeed.getUrl(), limit);
			} else {
				return feedService.getPagedFeed(rssFeed.getUrl(), cursor, limit);
			}
		} catch (Exception e) {
			throw new CustomException(PostErrorCode.NEWS_GET_FAILED);
		}
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
		String instructionPrompt = createPostPrompt.getInstruction();

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
	 * 단일 게시물을 prompt를 적용하여 업데하는 메서드
	 * @param request
	 * @param agentId
	 * @param postGroupId
	 * @param postId
	 * @return
	 */
	public PostResponse updateSinglePostByPrompt(SinglePostUpdateRequest request, Long agentId, Long postGroupId, Long postId) {
		// post 찾기
		Post post = postTransactionService.getPostOrThrow(postId);

		// 이전 응답값, 프롬프트
		String previousResponse = post.getContent();
		String prompt = request.prompt();

		// 응답 생성 -이전 응답을 assistant, 새로운 프롬프트를 user에 넣음
		ChatCompletionResponse result = applyPrompt(prompt, previousResponse);

		// JSON으로 파싱하여 새로운 요약과 본문 생성
		SummaryContentFormat newContent = parseSummaryContentFormat(
			result.getChoices().get(0).getMessage().getContent());

		return postTransactionService.updatePostAndPromptyHistory(post, prompt, newContent);
	}

	public List<PostResponse> updateMultiplePostsByPrompt(MultiplePostUpdateRequest request, Long agentId, Long postGroupId) {

		// 요청에서 postIds와 prompt 추출
		List<Long> postIds = request.postsId();
		String prompt = request.prompt();

		// postId 리스트를 기반으로 해당하는 게시물 조회
		List<Post> posts = postIds.stream()
			.map(postTransactionService::getPostOrThrow)
			.toList();

		// 각 게시물에 대해 ChatGPT 프롬프트 적용 및 업데이트
		return posts.stream().map(post -> {
			String previousResponse = post.getContent();

			ChatCompletionResponse result = applyPrompt(prompt, previousResponse);

			SummaryContentFormat newContent = parseSummaryContentFormat(
				result.getChoices().get(0).getMessage().getContent());

			return postTransactionService.updatePostAndPromptyHistory(post, prompt, newContent);
		}).toList();
	}


	/**
	 * postGroupId를 바탕으로 게시물 그룹 존재 여부를 확인하고, 해당 그룹의 게시물 목록을 반환하는 메서드
	 * 게시물 그룹 조회 실패 시 POST_GROUP_NOT_FOUND
	 */
	public List<PostResponse> getPostsByPostGroup(Long postGroupId) {
		// PostGroup 엔티티 조회
		PostGroup postGroup = postGroupRepository.findById(postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));

		// Post 엔티티 리스트 조회
		List<Post> posts = postRepository.findAllByPostGroup(postGroup);

		// 결과 반환
		return posts.stream()
			.map(PostResponse::from)
			.toList();
	}

	/**
	 * 게시물 수정 메서드. updateType에 따라 분기
	 */
	public void updatePost(Long postGroupId, Long postId, UpdatePostBasicRequest request) {
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

		// 수정 타입 검증
		switch (request.getUpdateType()) {
			case STATUS -> {
				if (request.getStatus() == null) {
					throw new CustomException(PostErrorCode.INVALID_UPDATING_POST_TYPE);
				}
				postTransactionService.updatePostStatus(post, request.getStatus());
			}
			case UPLOAD_TIME -> {
				if (request.getUploadTime() == null) {
					throw new CustomException(PostErrorCode.INVALID_UPDATING_POST_TYPE);
				}
				postTransactionService.updatePostUploadTime(post, request.getUploadTime());
			}
			case CONTENT -> {
				if (request.getContent() == null) {
					throw new CustomException(PostErrorCode.INVALID_UPDATING_POST_TYPE);
				}
				postTransactionService.updatePostContent(post, request.getContent());
			}
			case CONTENT_WITH_IMAGE -> {
				if (request.getContent() == null || request.getImageUrls() == null) {
					throw new CustomException(PostErrorCode.INVALID_UPDATING_POST_TYPE);
				}
				updatePostContentWithImage(post, request);
			}
		}
	}

	/**
	 * 게시물의 내용과 이미지를 수정하는 메서드
	 */
	private void updatePostContentWithImage(Post post, UpdatePostBasicRequest request) {
		// Post 엔티티 수정
		postTransactionService.updatePostContent(post, request.getContent());

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
	 * 게시물 일괄 수정 메서드. updateType에 따라 분기
	 */
	public void updatePosts(Long postGroupId, UpdatePostsBasicRequest request) {
		// PostGroup 엔티티 조회
		PostGroup postGroup = postGroupRepository.findById(postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));

		// Post 엔티티 리스트 조회
		List<Long> postIds = request.getPosts().stream()
			.map(UpdatePostsBasicRequestItem::getPostId)
			.toList();
		List<Post> posts = postRepository.findAllById(postIds);

		// 검증: PostGroup에 해당하는 Post가 맞는지 검증
		posts.forEach(post -> {
			if (!post.getPostGroup().getId().equals(postGroupId)) {
				throw new CustomException(PostErrorCode.INVALID_POST);
			}
		});

		// 수정 타입에 따라 분기
		switch (request.getUpdateType()) {
			case STATUS -> request.getPosts()
				.forEach(postRequest -> {
					Post post = postRepository.findById(postRequest.getPostId())  // 1차 캐시 조회
						.orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));
					if (postRequest.getStatus() == null) {
						throw new CustomException(PostErrorCode.INVALID_UPDATING_POST_TYPE);
					}
					postTransactionService.updatePostStatus(post, postRequest.getStatus());
				});
			case UPLOAD_TIME -> request.getPosts()
				.forEach(postRequest -> {
					Post post = postRepository.findById(postRequest.getPostId())  // 1차 캐시 조회
						.orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));
					if (postRequest.getUploadTime() == null) {
						throw new CustomException(PostErrorCode.INVALID_UPDATING_POST_TYPE);
					}
					postTransactionService.updatePostUploadTime(post, postRequest.getUploadTime());
				});
		}
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
