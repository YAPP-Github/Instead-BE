package org.mainapplication.domain.post.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.domainmodule.post.entity.Post;
import org.domainmodule.post.entity.type.PostStatusType;
import org.domainmodule.post.repository.PostRepository;
import org.domainmodule.postgroup.entity.PostGroup;
import org.domainmodule.postgroup.repository.PostGroupRepository;
import org.feedclient.service.FeedService;
import org.mainapplication.domain.post.controller.request.CreatePostsRequest;
import org.mainapplication.domain.post.controller.response.CreatePostsResponse;
import org.mainapplication.domain.post.controller.response.type.PostResponse;
import org.mainapplication.domain.post.prompt.PromptUtil;
import org.mainapplication.domain.post.prompt.ResponseContent;
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
	private final PromptUtil promptUtil;
	private final PostGroupRepository postGroupRepository;
	private final PostRepository postRepository;

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

	public CreatePostsResponse createPosts(CreatePostsRequest request) {
		// 프롬프트 생성
		String instructionPrompt = promptUtil.getInstruction();
		String topicPrompt = promptUtil.getBasicTopicPrompt(request);

		// 게시물 생성
		ArrayList<RequestMessage> messages = new ArrayList<>();
		messages.add(new RequestMessage("system", instructionPrompt));
		messages.add(new RequestMessage("user", topicPrompt));
		ChatCompletionResponse result = openAiClient.getChatCompletion(
			new ChatCompletionRequest(openAiModel, messages, responseFormat, 5, 0.7));

		System.out.println("open ai 결과 확인:");
		result.getChoices().forEach(choice -> {
			System.out.println(choice.getMessage().getContent());
		});

		// 게시물 그룹 및 게시물 저장
		PostGroup postGroup = postGroupRepository.save(PostGroup.createPostGroup(
			null,
			null,
			request.getTopic(),
			request.getPurpose(),
			request.getReference(),
			request.getLength(),
			request.getContent()
		));
		List<Post> posts = result.getChoices().stream()
			.map(choice -> {
				try {
					ResponseContent content = objectMapper.readValue(choice.getMessage().getContent(),
						ResponseContent.class);
					return postRepository.save(
						Post.createPost(postGroup, null, content.getSummary(), content.getContent(),
							PostStatusType.GENERATED, null));
				} catch (JsonProcessingException e) {
					return null;
				}
			})
			.toList();

		System.out.println("db 결과 확인: ");
		System.out.println(postGroup.toString());
		posts.forEach(post -> {
			System.out.println(post.toString());
		});

		// 결과 반환
		List<PostResponse> postResponses = posts.stream()
			.map(PostResponse::from)
			.toList();
		return new CreatePostsResponse(postGroup.getId(), null, postResponses);
	}

	public void createPostsByNews() {
		// 프롬프트 생성

		// 피드 받아오기

		// 게시물 생성하기

		// 게시물 저장하기

		// 결과 반환하기
	}

	public void createPostsByImage() {
		// 프롬프트 생성

		// 게시물 생성

		// 게시물 저장

		// 결과 반환
	}
}
