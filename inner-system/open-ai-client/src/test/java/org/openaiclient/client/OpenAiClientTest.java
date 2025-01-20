package org.openaiclient.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openaiclient.client.dto.request.ChatCompletionRequest;
import org.openaiclient.client.dto.request.type.RequestMessage;
import org.openaiclient.client.dto.request.type.ResponseFormat;
import org.openaiclient.client.dto.response.ChatCompletionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class OpenAiClientTest {

	@Autowired
	OpenAiClient openAiClient;

	@Test
	void getChatCompletionTest() throws JsonProcessingException {
		// Given
		String model = "gpt-4o-mini";

		ArrayList<RequestMessage> messages = new ArrayList<>();
		messages.add(new RequestMessage("user", "내일 점심 메뉴를 추천해줘"));
		messages.add(new RequestMessage("system",
			"user의 질문에 대해 2-3문장 정도로 content를 답변해주고, content에 대한 핵심 주제 요약인 summary를 명사형으로 함께 답변해줘. JSON 형태로 답변해야 해."));
		ResponseFormat responseFormat = new ResponseFormat("json_schema", Map.of(
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

		// When
		ChatCompletionResponse result = openAiClient.getChatCompletion(
			new ChatCompletionRequest(model, messages, responseFormat)
		);

		// Then
		System.out.println(result.getChoices().get(0).getMessage().getRole());
		System.out.println(result.getChoices().get(0).getMessage().getContent());
		ObjectMapper objectMapper = new ObjectMapper();
		TestResponse content = objectMapper.readValue(
			result.getChoices().get(0).getMessage().getContent(),
			TestResponse.class
		);
		System.out.println(content.getSummary());
		System.out.println(content.getContent());
		Assertions.assertAll(
			() -> Assertions.assertFalse(result.getChoices().isEmpty()),
			() -> Assertions.assertInstanceOf(TestResponse.class, content)
		);
	}

	@Test
	void getChatCompletionAsyncTest() throws JsonProcessingException {
		// Given
		String model = "gpt-4o-mini";

		ArrayList<RequestMessage> messages = new ArrayList<>();
		messages.add(new RequestMessage("user", "내일 점심 메뉴를 추천해줘"));
		messages.add(new RequestMessage("system",
			"user의 질문에 대해 2-3문장 정도로 content를 답변해주고, content에 대한 핵심 주제 요약인 summary를 명사형으로 함께 답변해줘. JSON 형태로 답변해야 해."));
		ResponseFormat responseFormat = new ResponseFormat("json_schema", Map.of(
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

		// When
		ChatCompletionResponse result = openAiClient.getChatCompletionAsync(
			new ChatCompletionRequest(model, messages, responseFormat)
		).join();

		// Then
		System.out.println(result.getChoices().get(0).getMessage().getRole());
		System.out.println(result.getChoices().get(0).getMessage().getContent());
		ObjectMapper objectMapper = new ObjectMapper();
		TestResponse content = objectMapper.readValue(
			result.getChoices().get(0).getMessage().getContent(),
			TestResponse.class
		);
		System.out.println(content.getSummary());
		System.out.println(content.getContent());
		Assertions.assertAll(
			() -> Assertions.assertFalse(result.getChoices().isEmpty()),
			() -> Assertions.assertInstanceOf(TestResponse.class, content)
		);
	}
}
