package org.mainapplication.domain.post.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
import org.mainapplication.domain.post.controller.request.SinglePostUpdateRequest;
import org.mainapplication.domain.post.controller.request.UpdatePostContentRequest;
import org.mainapplication.domain.post.controller.request.UpdatePostsRequest;
import org.mainapplication.domain.post.controller.request.type.UpdatePostsRequestItem;
import org.mainapplication.domain.post.controller.response.CreatePostsResponse;
import org.mainapplication.domain.post.controller.response.GetPostGroupPostsResponse;
import org.mainapplication.domain.post.controller.response.type.PostResponse;
import org.mainapplication.domain.post.exception.PostErrorCode;
import org.mainapplication.domain.post.service.dto.SavePostGroupAndPostsDto;
import org.mainapplication.domain.post.service.dto.SavePostGroupWithImagesAndPostsDto;
import org.mainapplication.domain.post.service.dto.SavePostGroupWithRssCursorAndPostsDto;
import org.mainapplication.domain.post.service.vo.GeneratePostsVo;
import org.mainapplication.global.constants.PostGenerationCount;
import org.mainapplication.global.error.CustomException;
import org.mainapplication.openai.contentformat.jsonschema.SummaryContentSchema;
import org.mainapplication.openai.contentformat.response.SummaryContentFormat;
import org.mainapplication.openai.prompt.CreatePostPrompt;
import org.openaiclient.client.OpenAiClient;
import org.openaiclient.client.dto.request.ChatCompletionRequest;
import org.openaiclient.client.dto.response.ChatCompletionResponse;
import org.openaiclient.client.dto.response.type.Choice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	 * 참고자료 없는 게시물 그룹과 게시물 생성 및 저장 메서드
	 */
	public CreatePostsResponse createPostsWithoutRef(CreatePostsRequest request, Integer limit) {
		// 게시물 생성
		ChatCompletionResponse result = generatePostsWithoutRef(GeneratePostsVo.of(request, limit));

		// PostGroup 엔티티 생성
		PostGroup postGroup = PostGroup.createPostGroup(null, null, request.getTopic(), request.getPurpose(),
			request.getReference(), request.getLength(), request.getContent(), 1);

		// Post 엔티티 생성: OpenAI API 응답의 choices에서 답변 꺼내 json으로 파싱 후 엔티티 생성
		// displayOrder 지정에 사용할 반복변수를 위해 for문 사용
		List<Post> posts = new ArrayList<>();
		for (int i = 0; i < result.getChoices().size(); i++) {
			Choice choice = result.getChoices().get(i);
			SummaryContentFormat content = parseSummaryContentFormat(choice.getMessage().getContent());
			posts.add(Post.create(postGroup, null, content.getSummary(),
				content.getContent(), PostStatusType.GENERATED, null, i + 1));
		}

		// PostGroup 및 Post 리스트 저장
		SavePostGroupAndPostsDto saveResult = postTransactionService.savePostGroupAndPosts(postGroup, posts);

		// 결과 반환
		List<PostResponse> postResponses = saveResult.posts().stream()
			.map(PostResponse::from)
			.toList();
		return new CreatePostsResponse(saveResult.postGroup().getId(), false, postResponses);
	}

	/**
	 * 뉴스 기사 기반 게시물 그룹과 게시물 생성 및 저장 메서드
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
			request.getReference(), request.getLength(), request.getContent(), 1);

		// PostGroupRssCursor 엔티티 생성
		String cursor = feedPagingResult.getFeedItems().get(feedPagingResult.getFeedItems().size() - 1).getId();
		PostGroupRssCursor postGroupRssCursor = PostGroupRssCursor.createPostGroupRssCursor(postGroup, cursor);

		// Post 엔티티 생성
		// displayOrder 지정에 사용할 반복변수를 위해 for문 사용
		List<Post> posts = new ArrayList<>();
		for (int i = 0; i < results.size(); i++) {
			ChatCompletionResponse result = results.get(i);
			SummaryContentFormat content = parseSummaryContentFormat(
				result.getChoices().get(0).getMessage().getContent());
			posts.add(Post.create(postGroup, null, content.getSummary(),
				content.getContent(), PostStatusType.GENERATED, null, i + 1));
		}

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
	 * 이미지 기반 게시물 그룹과 게시물 생성 및 저장 메서드
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
			request.getReference(), request.getLength(), request.getContent(), 1);

		// PostGroupImage 엔티티 리스트 생성
		List<PostGroupImage> postGroupImages = request.getImageUrls().stream()
			.map(imageUrl -> PostGroupImage.createPostGroupImage(postGroup, imageUrl))
			.toList();

		// Post 엔티티 리스트 생성
		// displayOrder 지정에 사용할 반복변수를 위해 for문 사용
		List<Post> posts = new ArrayList<>();
		for (int i = 0; i < result.getChoices().size(); i++) {
			Choice choice = result.getChoices().get(i);
			SummaryContentFormat content = parseSummaryContentFormat(choice.getMessage().getContent());
			posts.add(Post.create(postGroup, null, content.getSummary(),
				content.getContent(), PostStatusType.GENERATED, null, i + 1));
		}

		// 엔티티 저장
		SavePostGroupWithImagesAndPostsDto saveResult = postTransactionService.savePostGroupWithImagesAndPosts(
			postGroup, postGroupImages, posts);

		// 결과 반환
		List<PostResponse> postResponses = saveResult.posts().stream()
			.map(PostResponse::from)
			.toList();
		return new CreatePostsResponse(saveResult.postGroup().getId(), false, postResponses);
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
			case NONE -> createAdditionalPostsWithoutRef(postGroup, limit, order);
			case NEWS -> createAdditionalPostsByNews(postGroup, limit, order);
			case IMAGE -> createAdditionalPostsByImage(postGroup, limit, order);
		};
	}

	/**
	 * 게시물 추가 생성: 참고자료 없는 게시물 생성 및 저장 메서드
	 */
	private CreatePostsResponse createAdditionalPostsWithoutRef(PostGroup postGroup, Integer limit, Integer order) {
		// 게시물 생성
		ChatCompletionResponse result = generatePostsWithoutRef(GeneratePostsVo.of(postGroup, limit));

		// Post 엔티티 리스트 생성
		// order 지정에 사용할 반복변수를 위해 for문 사용
		List<Post> posts = new ArrayList<>();
		for (int i = 0; i < result.getChoices().size(); i++) {
			Choice choice = result.getChoices().get(i);
			SummaryContentFormat content = parseSummaryContentFormat(choice.getMessage().getContent());
			posts.add(Post.create(postGroup, null, content.getSummary(),
				content.getContent(), PostStatusType.GENERATED, null, order + i + 1));
		}

		// Post 엔티티 리스트 저장
		List<Post> savedPosts = postTransactionService.savePosts(posts);

		// PostGroup 엔티티의 생성 횟수 수정
		postGroup.increaseGenerationCount();
		postTransactionService.savePostGroup(postGroup);

		// eof 판단
		boolean eof = postGroup.getGenerationCount() >= PostGenerationCount.MAX_POST_GENERATION_COUNT;

		// 결과 반환
		List<PostResponse> postResponses = savedPosts.stream()
			.map(PostResponse::from)
			.toList();
		return new CreatePostsResponse(postGroup.getId(), eof, postResponses);
	}

	/**
	 * 게시물 추가 생성: 뉴스 기사 기반 게시물 생성 및 저장 메서드
	 * 피드 고갈 시 NEWS_FEED_EXHAUSTED
	 */
	private CreatePostsResponse createAdditionalPostsByNews(PostGroup postGroup, Integer limit, Integer order) {
		// 피드 받아오기: RssFeed와 PostGroupRssCursor를 DB에서 조회
		RssFeed rssFeed = rssFeedRepository.findByCategory(postGroup.getFeed().getCategory())
			.orElseThrow(() -> new CustomException(PostErrorCode.RSS_FEED_NOT_FOUND));
		PostGroupRssCursor rssCursor = postGroupRssCursorRepository.findByPostGroup(postGroup)
			.orElseThrow(() -> new CustomException(PostErrorCode.RSS_CURSOR_NOT_FOUND));
		FeedPagingResult feedPagingResult = getPagedNews(rssFeed, rssCursor.getNewsId(), limit);

		// 피드가 고갈된 경우 에러 응답
		if (feedPagingResult.getFeedItems().isEmpty()) {
			throw new CustomException(PostErrorCode.EXHAUSTED_NEWS_FEED);
		}

		// 게시물 생성
		List<ChatCompletionResponse> results = generatePostsByNews(
			GeneratePostsVo.of(postGroup, limit), feedPagingResult);

		// PostGroupRssCursor 업데이트
		String cursor = feedPagingResult.getFeedItems().get(feedPagingResult.getFeedItems().size() - 1).getId();
		rssCursor.updateNewsId(cursor);

		// Post 엔티티 생성
		// order 지정에 사용할 반복변수를 위해 for문 사용
		List<Post> posts = new ArrayList<>();
		for (int i = 0; i < results.size(); i++) {
			ChatCompletionResponse result = results.get(i);
			SummaryContentFormat content = parseSummaryContentFormat(
				result.getChoices().get(0).getMessage().getContent());
			posts.add(Post.create(postGroup, null, content.getSummary(),
				content.getContent(), PostStatusType.GENERATED, null, order + i + 1));
		}

		// 엔티티 저장
		List<Post> savedPosts = postTransactionService.savePosts(posts);

		// PostGroup 엔티티의 생성 횟수 수정
		postGroup.increaseGenerationCount();
		postTransactionService.savePostGroup(postGroup);

		// eof 판단
		System.out.println((postGroup.getGenerationCount() >= PostGenerationCount.MAX_POST_GENERATION_COUNT));
		System.out.println(feedPagingResult.isEof());
		boolean eof = (postGroup.getGenerationCount() >= PostGenerationCount.MAX_POST_GENERATION_COUNT)
			|| (feedPagingResult.isEof());

		// 결과 반환하기
		List<PostResponse> postResponses = savedPosts.stream()
			.map(PostResponse::from)
			.toList();
		return new CreatePostsResponse(postGroup.getId(), eof, postResponses);
	}

	/**
	 * 게시물 추가 생성: 이미지 기반 게시물 생성 및 저장 메서드
	 */
	private CreatePostsResponse createAdditionalPostsByImage(PostGroup postGroup, Integer limit, Integer order) {
		// 게시물 생성
		ChatCompletionResponse result = generatePostsByImage(GeneratePostsVo.of(postGroup, limit));

		// Post 엔티티 리스트 생성
		// order 지정에 사용할 반복변수를 위해 for문 사용
		List<Post> posts = new ArrayList<>();
		for (int i = 0; i < result.getChoices().size(); i++) {
			Choice choice = result.getChoices().get(i);
			SummaryContentFormat content = parseSummaryContentFormat(choice.getMessage().getContent());
			posts.add(Post.create(postGroup, null, content.getSummary(),
				content.getContent(), PostStatusType.GENERATED, null, order + i + 1));
		}

		// 엔티티 저장
		List<Post> savedPosts = postTransactionService.savePosts(posts);

		// PostGroup 엔티티의 생성 횟수 수정
		postGroup.increaseGenerationCount();
		postTransactionService.savePostGroup(postGroup);

		// eof 판단
		boolean eof = (postGroup.getGenerationCount() >= PostGenerationCount.MAX_POST_GENERATION_COUNT);

		// 결과 반환
		List<PostResponse> postResponses = savedPosts.stream()
			.map(PostResponse::from)
			.toList();
		return new CreatePostsResponse(postGroup.getId(), eof, postResponses);
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
	 * @return
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
	 * @return
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
