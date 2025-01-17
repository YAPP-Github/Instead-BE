package org.openaiclient.client;

import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openaiclient.client.dto.request.ChatCompletionRequest;
import org.openaiclient.client.dto.request.type.RequestMessage;
import org.openaiclient.client.dto.response.ChatCompletionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class OpenAiClientTest {

	@Autowired
	OpenAiClient openAiClient;

	@Test
	void getChatCompletionTest() {
		// Given
		String model = "gpt-4o-mini";
		RequestMessage message = new RequestMessage("user", "내일 점심 메뉴를 추천해줘");
		ArrayList<RequestMessage> messages = new ArrayList<>();
		messages.add(message);

		// When
		ChatCompletionResponse result = openAiClient.getChatCompletion(
			new ChatCompletionRequest(model, messages));

		// Then
		System.out.println(result.getChoices().get(0).getMessage().getRole());
		System.out.println(result.getChoices().get(0).getMessage().getContent());
		Assertions.assertAll(
			() -> Assertions.assertFalse(result.getChoices().isEmpty())
		);
	}

	@Test
	void getChatCompletionAsyncTest() {
		// Given
		String model = "gpt-4o-mini";
		RequestMessage message = new RequestMessage("user", "내일 점심 메뉴를 추천해줘");
		ArrayList<RequestMessage> messages = new ArrayList<>();
		messages.add(message);

		// When
		ChatCompletionResponse result = openAiClient.getChatCompletionAsync(
			new ChatCompletionRequest(model, messages)).join();

		// Then
		System.out.println(result.getChoices().get(0).getMessage().getRole());
		System.out.println(result.getChoices().get(0).getMessage().getContent());
		Assertions.assertAll(
			() -> Assertions.assertFalse(result.getChoices().isEmpty())
		);
	}
}
