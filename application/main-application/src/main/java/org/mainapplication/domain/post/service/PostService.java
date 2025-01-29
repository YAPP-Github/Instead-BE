package org.mainapplication.domain.post.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.domainmodule.post.entity.Post;
import org.domainmodule.post.entity.type.PostStatusType;
import org.domainmodule.postgroup.entity.PostGroup;
import org.domainmodule.postgroup.entity.PostGroupImage;
import org.domainmodule.rssfeed.entity.RssFeed;
import org.domainmodule.rssfeed.exception.RssFeedNotFoundException;
import org.domainmodule.rssfeed.repository.RssFeedRepository;
import org.feedclient.service.FeedService;
import org.feedclient.service.dto.FeedPagingResult;
import org.mainapplication.domain.post.controller.request.CreatePostsRequest;
import org.mainapplication.domain.post.controller.response.CreatePostsResponse;
import org.mainapplication.domain.post.controller.response.type.PostResponse;
import org.mainapplication.domain.post.exception.PostErrorCode;
import org.mainapplication.domain.post.service.dto.SavePostGroupAndPostsDto;
import org.mainapplication.domain.post.service.dto.SavePostGroupWithImagesAndPostsDto;
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

	@Value("${client.openai.model}")
	private String openAiModel;

	/**
	 * 참고자료 없는 게시물 생성 메서드
	 */
	public CreatePostsResponse createPostsWithoutRef(CreatePostsRequest request, Integer limit) {
		// 프롬프트 생성: Instruction + 주제 Prompt
		String instructionPrompt = createPostPrompt.getInstruction();
		String topicPrompt = createPostPrompt.getBasicTopicPrompt(request);

		// 게시물 생성
		ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest(openAiModel,
			summaryContentSchema.getResponseFormat(), limit, null)
			.addDeveloperMessage(instructionPrompt)
			.addUserTextMessage(topicPrompt);
		ChatCompletionResponse result = generatePosts(chatCompletionRequest);

		// PostGroup 엔티티 생성
		PostGroup postGroup = PostGroup.createPostGroup(null, null, request.getTopic(), request.getPurpose(),
			request.getReference(), request.getLength(), request.getContent());

		// Post 엔티티 생성: OpenAI API 응답의 choices에서 답변 꺼내 json으로 파싱 후 엔티티 생성
		List<Post> posts = result.getChoices().stream()
			.map(choice -> {
				SummaryContentFormat content = parseSummaryContentFormat(choice.getMessage().getContent());
				return Post.createPost(postGroup, null, content.getSummary(), content.getContent(),
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
	 * 뉴스 기사 기반 게시물 생성 메서드
	 */
	public CreatePostsResponse createPostsByNews(CreatePostsRequest request, Integer limit) {
		// 피드 받아오기
		RssFeed rssFeed = rssFeedRepository.findByCategory(request.getNewsCategory())
			.orElseThrow(() -> new RssFeedNotFoundException("뉴스 카테고리를 찾을 수 없습니다."));
		FeedPagingResult feedPagingResult = getNewsFeed(rssFeed, limit);

		// 프롬프트 생성
		String instructionPrompt = createPostPrompt.getInstruction();
		String topicPrompt = createPostPrompt.getBasicTopicPrompt(request);
		List<String> refPrompts = feedPagingResult.getFeedItems().stream()
			.map(news -> createPostPrompt.getNewsRefPrompt(news.getContentSummary(), news.getContent()))
			.toList();

		// 게시물 생성하기: 각 뉴스 기사별로 OpenAI API 호출 및 답변 생성
		List<CompletableFuture<ChatCompletionResponse>> resultFutures = refPrompts.stream()
			.map(refPrompt -> generatePostsAsync(
				new ChatCompletionRequest(openAiModel, summaryContentSchema.getResponseFormat(), null, null)
					.addDeveloperMessage(instructionPrompt)
					.addUserTextMessage(topicPrompt)
					.addUserTextMessage(refPrompt)
			))
			.toList();
		List<ChatCompletionResponse> results = resultFutures.stream()
			.map(CompletableFuture::join)
			.toList();

		// PostGroup 엔티티 생성
		PostGroup postGroup = PostGroup.createPostGroup(null, rssFeed, request.getTopic(), request.getPurpose(),
			request.getReference(), request.getLength(), request.getContent());

		// Post 엔티티 생성
		List<Post> posts = results.stream()
			.map(result -> {
				SummaryContentFormat content = parseSummaryContentFormat(
					result.getChoices().get(0).getMessage().getContent());
				return Post.createPost(postGroup, null, content.getSummary(), content.getContent(),
					PostStatusType.GENERATED, null);
			})
			.toList();

		// PostGroup 및 Post 리스트 엔티티 저장
		SavePostGroupAndPostsDto saveResult = postTransactionService.savePostGroupAndPosts(postGroup, posts);

		// 결과 반환하기
		List<PostResponse> postResponses = saveResult.posts().stream()
			.map(PostResponse::from)
			.toList();
		return new CreatePostsResponse(saveResult.postGroup().getId(), feedPagingResult.isEof(), postResponses);
	}

	/**
	 * 이미지 기반 게시물 생성 메서드
	 */
	public CreatePostsResponse createPostsByImage(CreatePostsRequest request, Integer limit) {
		// 프롬프트 생성
		String instructionPrompt = createPostPrompt.getInstruction();
		String topicPrompt = createPostPrompt.getBasicTopicPrompt(request);
		String imageRefPrompt = createPostPrompt.getImageRefPrompt();

		// 게시물 생성
		ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest(openAiModel,
			summaryContentSchema.getResponseFormat(), limit, null)
			.addDeveloperMessage(instructionPrompt)
			.addUserTextMessage(topicPrompt)
			.addUserImageMessage(imageRefPrompt, request.getImageUrls());
		ChatCompletionResponse result = generatePosts(chatCompletionRequest);

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
				return Post.createPost(postGroup, null, content.getSummary(), content.getContent(),
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
	 * OpenAiClient의 getChatCompletion 메서드를 호출하여 게시물 생성을 요청하는 메서드
	 * 예외 발생 시 PostGenerateFailedException 발생
	 */
	private ChatCompletionResponse generatePosts(ChatCompletionRequest request) {
		try {
			return openAiClient.getChatCompletion(request);
		} catch (RuntimeException e) {
			throw new CustomException(PostErrorCode.POST_GENERATE_FAILED);
		}
	}

	/**
	 * OpenAiClient의 getChatCompletionAsync 메서드를 호출하여 게시물 생성을 요청하는 메서드
	 * 예외 발생 시 PostGenerateFailedException
	 */
	private CompletableFuture<ChatCompletionResponse> generatePostsAsync(ChatCompletionRequest request) {
		try {
			return openAiClient.getChatCompletionAsync(request);
		} catch (RuntimeException e) {
			throw new CustomException(PostErrorCode.POST_GENERATE_FAILED);
		}
	}

	/**
	 * 요청한 뉴스 카테고리에 따라 뉴스 피드를 가져오는 메서드
	 * 피드 가져오기 실패 시 NewsGetFailedException
	 */
	private FeedPagingResult getNewsFeed(RssFeed rssFeed, Integer limit) {
		try {
			return feedService.getPagedFeed(rssFeed.getUrl(), limit);
		} catch (Exception e) {
			throw new CustomException(PostErrorCode.NEWS_GET_FAILED);
		}
	}

	/**
	 * OpenAI API의 응답 content를 SummaryContentFormat 형태로 파싱하는 메서드
	 * 파싱에 실패한 경우 대체 content를 반환
	 */
	private SummaryContentFormat parseSummaryContentFormat(String content) {
		try {
			return objectMapper.readValue(content, SummaryContentFormat.class);
		} catch (JsonProcessingException e) {
			return SummaryContentFormat.createAlternativeFormat("생성된 게시물", content);
		}
	}
}
