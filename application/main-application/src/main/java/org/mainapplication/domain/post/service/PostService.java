package org.mainapplication.domain.post.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.domainmodule.post.entity.Post;
import org.domainmodule.post.entity.type.PostStatusType;
import org.domainmodule.postgroup.entity.PostGroup;
import org.domainmodule.rssfeed.entity.RssFeed;
import org.domainmodule.rssfeed.repository.RssFeedRepository;
import org.feedclient.service.FeedService;
import org.feedclient.service.dto.FeedPagingResult;
import org.mainapplication.domain.post.controller.request.CreatePostsRequest;
import org.mainapplication.domain.post.controller.response.CreatePostsResponse;
import org.mainapplication.domain.post.controller.response.type.PostResponse;
import org.mainapplication.domain.post.prompt.PromptUtil;
import org.mainapplication.domain.post.prompt.ResponseContent;
import org.mainapplication.domain.post.service.dto.SavePostGroupAndPostDto;
import org.openaiclient.client.OpenAiClient;
import org.openaiclient.client.dto.request.ChatCompletionRequest;
import org.openaiclient.client.dto.request.type.RequestMessage;
import org.openaiclient.client.dto.request.type.ResponseFormat;
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
	private final PromptUtil promptUtil;
	private final RssFeedRepository rssFeedRepository;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Value("${client.openai.model}")
	private String openAiModel;

	// TODO: 이건 임시 response format이고, 실제 format은 json 파일로 관리할 예정
	private final ResponseFormat responseFormat = new ResponseFormat("json_schema", Map.of(
		"name", "test_response",
		"strict", true,
		"schema", Map.of(
			"type", "object",
			"properties", Map.of(
				"summary", Map.of(
					"type", "string",
					"description", "summary of main content"
				),
				"content", Map.of(
					"type", "string",
					"description", "main content"
				)
			),
			"additionalProperties", false,
			"required", List.of("summary", "content")
		)
	));

	/**
	 * 참고자료 없는 게시물 생성 메서드
	 */
	public CreatePostsResponse createPosts(CreatePostsRequest request, Integer limit) {
		// 프롬프트 생성: Instruction + 주제 Prompt
		String instructionPrompt = promptUtil.getInstruction();
		String topicPrompt = promptUtil.getBasicTopicPrompt(request);

		// 게시물 생성
		ArrayList<RequestMessage> messages = new ArrayList<>();
		messages.add(new RequestMessage("system", instructionPrompt));
		messages.add(new RequestMessage("user", topicPrompt));
		ChatCompletionResponse result = openAiClient.getChatCompletion(
			new ChatCompletionRequest(openAiModel, messages, responseFormat, limit, 0.7));

		// PostGroup 엔티티 생성
		PostGroup postGroup = PostGroup.createPostGroup(null, null, request.getTopic(), request.getPurpose(),
			request.getReference(),
			request.getLength(), request.getContent());

		// Post 엔티티 생성: OpenAI API 응답의 choices에서 답변 꺼내 json으로 파싱 후 엔티티 생성
		List<Post> posts = result.getChoices().stream()
			.map(choice -> {
				try {
					ResponseContent content = objectMapper.readValue(choice.getMessage().getContent(),
						ResponseContent.class);
					return Post.createPost(postGroup, null, content.getSummary(), content.getContent(),
						PostStatusType.GENERATED, null);
				} catch (JsonProcessingException e) {
					return null;
				}
			})
			.toList();

		// PostGroup 및 Post 리스트 저장
		SavePostGroupAndPostDto saveResult = postTransactionService.savePostGroupAndPosts(postGroup, posts);

		// 결과 반환
		List<PostResponse> postResponses = saveResult.posts().stream()
			.map(PostResponse::from)
			.toList();
		return new CreatePostsResponse(saveResult.postGroup().getId(), null, postResponses);
	}

	public CreatePostsResponse createPostsByNews(CreatePostsRequest request, Integer limit) {
		// 피드 받아오기
		RssFeed rssFeed = rssFeedRepository.findByCategory(request.getNewsCategory())
			.orElseThrow(() -> new RuntimeException("뉴스 카테고리를 찾을 수 없습니다."));
		FeedPagingResult feedPagingResult = feedService.getPagedFeed(rssFeed.getUrl(), limit);

		// 프롬프트 생성
		String instructionPrompt = promptUtil.getInstruction();
		String topicPrompt = promptUtil.getBasicTopicPrompt(request);
		List<String> refPrompts = feedPagingResult.getFeedItems().stream()
			.map(news -> promptUtil.getNewsRefPrompt(news.getContentSummary(), news.getContent()))
			.toList();

		// 게시물 생성하기: 각 뉴스 기사별로 OpenAI API 호출 및 답변 생성
		RequestMessage instructionMessage = new RequestMessage("system", instructionPrompt);
		RequestMessage topicMessage = new RequestMessage("user", topicPrompt);
		List<CompletableFuture<ChatCompletionResponse>> resultFutures = refPrompts.stream()
			.map(refPrompt -> {
				ArrayList<RequestMessage> messages = new ArrayList<>();
				messages.add(instructionMessage);
				messages.add(topicMessage);
				messages.add(new RequestMessage("user", refPrompt));
				return openAiClient.getChatCompletionAsync(
					new ChatCompletionRequest(openAiModel, messages, responseFormat, null, null));
			})
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
				try {
					ResponseContent content = objectMapper.readValue(
						result.getChoices().get(0).getMessage().getContent(), ResponseContent.class);
					return Post.createPost(postGroup, null, content.getSummary(), content.getContent(),
						PostStatusType.GENERATED, null);
				} catch (JsonProcessingException e) {
					return null;
				}
			})
			.toList();

		// PostGroup 및 Post 리스트 엔티티 저장
		SavePostGroupAndPostDto saveResult = postTransactionService.savePostGroupAndPosts(postGroup, posts);

		System.out.println("db 저장 결과:");
		saveResult.posts().forEach(System.out::println);

		// 결과 반환하기
		List<PostResponse> postResponses = saveResult.posts().stream()
			.map(PostResponse::from)
			.toList();
		return new CreatePostsResponse(saveResult.postGroup().getId(), feedPagingResult.isEof(), postResponses);
	}

	public void createPostsByImage() {
		// 프롬프트 생성

		// 게시물 생성

		// 게시물 저장

		// 결과 반환
	}
}
