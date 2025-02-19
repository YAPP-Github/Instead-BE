package org.mainapp.domain.post.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.domainmodule.agent.entity.AgentPersonalSetting;
import org.domainmodule.post.entity.Post;
import org.domainmodule.post.entity.type.PostStatusType;
import org.domainmodule.post.repository.PostRepository;
import org.domainmodule.postgroup.entity.PostGroup;
import org.domainmodule.postgroup.entity.PostGroupImage;
import org.domainmodule.postgroup.entity.PostGroupRssCursor;
import org.domainmodule.postgroup.repository.PostGroupImageRepository;
import org.domainmodule.postgroup.repository.PostGroupRssCursorRepository;
import org.domainmodule.rssfeed.entity.RssFeed;
import org.domainmodule.rssfeed.repository.RssFeedRepository;
import org.feedclient.service.FeedService;
import org.feedclient.service.dto.FeedPagingResult;
import org.mainapp.domain.post.controller.request.CreatePostsRequest;
import org.mainapp.domain.post.controller.response.CreatePostsResponse;
import org.mainapp.domain.post.controller.response.type.PostResponse;
import org.mainapp.domain.post.exception.PostErrorCode;
import org.mainapp.domain.post.service.dto.SavePostGroupAndPostsDto;
import org.mainapp.domain.post.service.dto.SavePostGroupWithImagesAndPostsDto;
import org.mainapp.domain.post.service.dto.SavePostGroupWithRssCursorAndPostsDto;
import org.mainapp.domain.post.service.vo.GeneratePostsVo;
import org.mainapp.global.constants.PostGenerationCount;
import org.mainapp.global.error.CustomException;
import org.mainapp.openai.contentformat.jsonschema.DetailTopicsSchema;
import org.mainapp.openai.contentformat.jsonschema.SummaryContentSchema;
import org.mainapp.openai.contentformat.response.DetailTopicsFormat;
import org.mainapp.openai.contentformat.response.SummaryContentFormat;
import org.mainapp.openai.prompt.CreateDetailTopicsPromptTemplate;
import org.mainapp.openai.prompt.CreatePostPromptTemplate;
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
public class PostCreateService {

	private final PostTransactionService postTransactionService;
	private final CreateDetailTopicsPromptTemplate createDetailTopicsPromptTemplate;
	private final CreatePostPromptTemplate createPostPromptTemplate;
	private final SummaryContentSchema summaryContentSchema;
	private final DetailTopicsSchema detailTopicsSchema;

	private final FeedService feedService;
	private final OpenAiClient openAiClient;

	private final PostGroupRssCursorRepository postGroupRssCursorRepository;
	private final PostGroupImageRepository postGroupImageRepository;
	private final RssFeedRepository rssFeedRepository;
	private final PostRepository postRepository;

	private final ObjectMapper objectMapper;

	@Value("${client.openai.model}")
	private String openAiModel;

	@Value("${default-image.post-group}")
	private String postGroupDefaultImage;

	/**
	 * 참고자료 없는 게시물 그룹과 게시물 생성 및 저장 메서드
	 */
	public CreatePostsResponse createPostsWithoutRef(
		AgentPersonalSetting agentPersonalSetting, CreatePostsRequest request, Integer limit
	) {
		// 세부 주제 생성
		List<String> topics = generateDetailTopics(request.getTopic(), null, limit);

		// 게시물 생성
		List<ChatCompletionResponse> results = generatePostsWithoutRef(
			GeneratePostsVo.of(agentPersonalSetting, request, limit), topics
		);

		// PostGroup 엔티티 생성: 생성 횟수 1로 초기화
		PostGroup postGroup = PostGroup.createPostGroup(agentPersonalSetting.getAgent(), null, request.getTopic(),
			request.getPurpose(),
			request.getReference(), request.getLength(), request.getContent(), 1, postGroupDefaultImage);

		// Post 엔티티 생성
		// displayOrder 지정에 사용할 반복변수를 위해 for문 사용
		List<Post> posts = new ArrayList<>();
		for (int i = 0; i < results.size(); i++) {
			ChatCompletionResponse result = results.get(i);
			SummaryContentFormat content = parseSummaryContentFormat(
				result.getChoices().get(0).getMessage().getContent());
			posts.add(Post.create(postGroup, topics.get(i), content.getSummary(),
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
	public CreatePostsResponse createPostsByNews(
		AgentPersonalSetting agentPersonalSetting, CreatePostsRequest request, Integer limit
	) {
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
			GeneratePostsVo.of(agentPersonalSetting, request, limit), feedPagingResult
		);

		// PostGroup 엔티티 생성
		PostGroup postGroup = PostGroup.createPostGroup(
			agentPersonalSetting.getAgent(), rssFeed, request.getTopic(), request.getPurpose(), request.getReference(),
			request.getLength(), request.getContent(), 1, postGroupDefaultImage
		);

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
			posts.add(Post.create(postGroup, content.getSummary(), content.getSummary(),
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
	public CreatePostsResponse createPostsByImage(
		AgentPersonalSetting agentPersonalSetting, CreatePostsRequest request, Integer limit
	) {
		// imageUrls 필드 검증
		if (request.getImageUrls() == null || request.getImageUrls().isEmpty()) {
			throw new CustomException(PostErrorCode.NO_IMAGE_URLS);
		}

		// 세부 주제 생성
		List<String> topics = generateDetailTopics(request.getTopic(), null, limit);

		// 게시물 생성
		List<ChatCompletionResponse> results = generatePostsByImage(
			GeneratePostsVo.of(agentPersonalSetting, request, limit), topics
		);

		// PostGroup 엔티티 생성
		PostGroup postGroup = PostGroup.createPostGroup(
			agentPersonalSetting.getAgent(), null, request.getTopic(), request.getPurpose(), request.getReference(),
			request.getLength(), request.getContent(), 1, request.getImageUrls().get(0)
		);

		// PostGroupImage 엔티티 리스트 생성
		List<PostGroupImage> postGroupImages = request.getImageUrls().stream()
			.map(imageUrl -> PostGroupImage.createPostGroupImage(postGroup, imageUrl))
			.toList();

		// Post 엔티티 리스트 생성
		// displayOrder 지정에 사용할 반복변수를 위해 for문 사용
		List<Post> posts = new ArrayList<>();
		for (int i = 0; i < results.size(); i++) {
			ChatCompletionResponse result = results.get(i);
			SummaryContentFormat content = parseSummaryContentFormat(
				result.getChoices().get(0).getMessage().getContent());
			posts.add(Post.create(postGroup, topics.get(i), content.getSummary(),
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
	 * 게시물 추가 생성: 참고자료 없는 게시물 생성 및 저장 메서드
	 */
	public CreatePostsResponse createAdditionalPostsWithoutRef(
		AgentPersonalSetting agentPersonalSetting, PostGroup postGroup, Integer limit, Integer order
	) {
		// 기존 게시물 조회 후 기존 요약제목 추출
		List<Post> existPosts = postRepository.findAllByPostGroup(postGroup);
		List<String> existTopics = existPosts.stream()
			.map(Post::getSummary)
			.toList();

		// 세부 주제 생성
		List<String> topics = generateDetailTopics(postGroup.getTopic(), existTopics, limit);

		// 게시물 생성
		List<ChatCompletionResponse> results = generatePostsWithoutRef(
			GeneratePostsVo.of(agentPersonalSetting, postGroup, limit), topics
		);

		// Post 엔티티 리스트 생성
		// order 지정에 사용할 반복변수를 위해 for문 사용
		List<Post> posts = new ArrayList<>();
		for (int i = 0; i < results.size(); i++) {
			ChatCompletionResponse result = results.get(i);
			SummaryContentFormat content = parseSummaryContentFormat(
				result.getChoices().get(0).getMessage().getContent());
			posts.add(Post.create(postGroup, topics.get(i), content.getSummary(),
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
	public CreatePostsResponse createAdditionalPostsByNews(
		AgentPersonalSetting agentPersonalSetting, PostGroup postGroup, Integer limit, Integer order
	) {
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
			GeneratePostsVo.of(agentPersonalSetting, postGroup, limit), feedPagingResult);

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
			posts.add(Post.create(postGroup, content.getSummary(), content.getSummary(),
				content.getContent(), PostStatusType.GENERATED, null, order + i + 1));
		}

		// 엔티티 저장
		List<Post> savedPosts = postTransactionService.savePosts(posts);

		// PostGroup 엔티티의 생성 횟수 수정
		postGroup.increaseGenerationCount();
		postTransactionService.savePostGroup(postGroup);

		// eof 판단
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
	public CreatePostsResponse createAdditionalPostsByImage(
		AgentPersonalSetting agentPersonalSetting, PostGroup postGroup, Integer limit, Integer order
	) {
		// PostGroupImage 리스트 조회
		List<PostGroupImage> postGroupImages = postGroupImageRepository.findAllByPostGroup(postGroup);

		// 기존 게시물 조회 후 기존 요약제목 추출
		List<Post> existPosts = postRepository.findAllByPostGroup(postGroup);
		List<String> existTopics = existPosts.stream()
			.map(Post::getSummary)
			.toList();

		// 세부 주제 생성
		List<String> topics = generateDetailTopics(postGroup.getTopic(), existTopics, limit);

		// 게시물 생성
		List<ChatCompletionResponse> results = generatePostsByImage(
			GeneratePostsVo.of(agentPersonalSetting, postGroup, postGroupImages, limit), topics);

		// Post 엔티티 리스트 생성
		// order 지정에 사용할 반복변수를 위해 for문 사용
		List<Post> posts = new ArrayList<>();
		for (int i = 0; i < results.size(); i++) {
			ChatCompletionResponse result = results.get(i);
			SummaryContentFormat content = parseSummaryContentFormat(
				result.getChoices().get(0).getMessage().getContent());
			posts.add(Post.create(postGroup, topics.get(i), content.getSummary(),
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
	 * 요청된 주제와 관련된 세부 주제를 생성하는 메서드.
	 */
	public List<String> generateDetailTopics(String topic, List<String> existTopics, Integer limit) {
		// chat completion 요청 객체 생성
		ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest(
			openAiModel, detailTopicsSchema.getResponseFormat(), null, null)
			.addUserTextMessage(createDetailTopicsPromptTemplate.getGenerateDetailTopicsPrompt(topic, limit))
			.addUserTextMessage(createDetailTopicsPromptTemplate.getExcludeExistTopicsPrompt(existTopics));

		// 응답 객체 파싱 및 반환. 답변 생성 실패할 경우 요청된 주제만 담긴 리스트 반환
		try {
			ChatCompletionResponse result = openAiClient.getChatCompletion(chatCompletionRequest);
			String content = result.getChoices().get(0).getMessage().getContent();
			return parseTopicsFormat(content).getTopics();
		} catch (RuntimeException e) {
			ArrayList<String> topics = new ArrayList<>();
			for (int i = 0; i < limit; i++) {
				topics.add(topic);
			}
			return topics;
		}
	}

	/**
	 * 참고자료 없는 게시물 생성 메서드. (프롬프트 설정 + 게시물 생성 작업 수행)
	 * 예외 발생 시 PostGenerateFailedException 발생
	 */
	private List<ChatCompletionResponse> generatePostsWithoutRef(GeneratePostsVo vo, List<String> topics) {
		// 프롬프트 생성: Instruction + 주제 Prompt 리스트
		String instructionPrompt = createPostPromptTemplate.getInstructionPrompt(
			vo.domain(), vo.introduction(), vo.tone(), vo.customTone());
		List<String> topicPrompts = topics.stream()
			.map(topic -> createPostPromptTemplate.getTopicPrompt(topic, vo.purpose(), vo.length(), vo.content()))
			.toList();

		// 게시물 생성
		List<CompletableFuture<ChatCompletionResponse>> resultFutures = topicPrompts.stream()
			.map(topicPrompt -> openAiClient.getChatCompletionAsync(
				new ChatCompletionRequest(openAiModel, summaryContentSchema.getResponseFormat(), null, null)
					.addDeveloperMessage(instructionPrompt)
					.addUserTextMessage(topicPrompt)
			))
			.toList();

		try {
			return resultFutures.stream()
				.map(CompletableFuture::join)
				.toList();
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
		String instructionPrompt = createPostPromptTemplate.getInstructionPrompt(
			vo.domain(), vo.introduction(), vo.tone(), vo.customTone());
		String topicPrompt = createPostPromptTemplate.getTopicPrompt(
			vo.topic(), vo.purpose(), vo.length(), vo.content());
		List<String> refPrompts = feedPagingResult.getFeedItems().stream()
			.map(news ->
				createPostPromptTemplate.getNewsRefPrompt(news.getTitle(), news.getContentSummary(), news.getContent()))
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
	private List<ChatCompletionResponse> generatePostsByImage(GeneratePostsVo vo, List<String> topics) {
		// 프롬프트 생성
		String instructionPrompt = createPostPromptTemplate.getInstructionPrompt(
			vo.domain(), vo.introduction(), vo.tone(), vo.customTone());
		List<String> topicPrompts = topics.stream()
			.map(topic -> createPostPromptTemplate.getTopicPrompt(topic, vo.purpose(), vo.length(), vo.content()))
			.toList();
		String imageRefPrompt = createPostPromptTemplate.getImageRefPrompt();

		// 게시물 생성
		List<CompletableFuture<ChatCompletionResponse>> resultFutures = topicPrompts.stream()
			.map(topicPrompt -> openAiClient.getChatCompletionAsync(
				new ChatCompletionRequest(openAiModel, summaryContentSchema.getResponseFormat(), null, null)
					.addDeveloperMessage(instructionPrompt)
					.addUserTextMessage(topicPrompt)
					.addUserImageMessage(imageRefPrompt, vo.imageUrls())
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

	private DetailTopicsFormat parseTopicsFormat(String content) {
		try {
			return objectMapper.readValue(content, DetailTopicsFormat.class);
		} catch (JsonProcessingException e) {
			return new DetailTopicsFormat();
			// return SummaryContentFormat.createAlternativeFormat("생성된 게시물", content);
		}
	}
}
