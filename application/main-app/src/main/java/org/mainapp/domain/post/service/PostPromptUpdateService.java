package org.mainapp.domain.post.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.domainmodule.post.entity.Post;
import org.mainapp.domain.post.controller.request.MultiplePostUpdateRequest;
import org.mainapp.domain.post.controller.request.SinglePostUpdateRequest;
import org.mainapp.domain.post.controller.response.type.PostResponse;
import org.mainapp.domain.post.exception.PostErrorCode;
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
public class PostPromptUpdateService {

	private final PostTransactionService postTransactionService;
	private final CreatePostPromptTemplate createPostPromptTemplate;
	private final SummaryContentSchema summaryContentSchema;

	private final OpenAiClient openAiClient;

	private final ObjectMapper objectMapper;

	@Value("${client.openai.model}")
	private String openAiModel;

	/**
	 * 단일 게시물을 prompt를 적용하여 업데이트하는 메서드
	 */
	@Transactional
	public PostResponse updateSinglePostByPrompt(Post post, SinglePostUpdateRequest request) {
		// 프롬프트 적용
		SummaryContentFormat newContent = updatePostContentByPrompt(post, request.prompt());
		// DB값 업데이트
		return postTransactionService.updateSinglePostAndPromptyHistory(post, request.prompt(), newContent);
	}

	/**
	 * 일괄로 게시물들을 prompt 적용 후 업데이트 하는 메서드
	 */
	public List<PostResponse> updateMultiplePostsByPrompt(List<Post> posts, MultiplePostUpdateRequest request) {
		// Post 리스트 업그레이드 비동기 작업 수행
		List<CompletableFuture<SummaryContentFormat>> futures = posts.stream()
			.map(post -> CompletableFuture.supplyAsync(() -> updatePostContentByPrompt(post, request.prompt())))
			.toList();

		// 모든 프롬프트 처리 완료 대기
		List<SummaryContentFormat> newContents = futures.stream()
			.map(CompletableFuture::join)
			.toList();

		// DB 업데이트 및 결과 반환
		return postTransactionService.updateMutiplePostAndPromptyHistory(posts, request.prompt(), newContents);
	}

	private SummaryContentFormat updatePostContentByPrompt(Post post, String prompt) {
		String previousResponse = post.getContent();

		// ChatGPT 프롬프트 실행
		ChatCompletionResponse result = applyPrompt(prompt, previousResponse);

		// 결과 파싱하여 새로운 요약 + 본문 생성
		return parseSummaryContentFormat(
			result.getChoices().get(0).getMessage().getContent()
		);
	}

	private ChatCompletionResponse applyPrompt(String prompt, String previousResponse) {
		// 프롬프트 생성: Instruction
		String instructionPrompt = createPostPromptTemplate.getTempInstructionPrompt();

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
}
