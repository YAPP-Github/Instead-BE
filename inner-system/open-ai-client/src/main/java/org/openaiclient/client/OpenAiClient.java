package org.openaiclient.client;

import java.util.concurrent.CompletableFuture;

import org.openaiclient.client.dto.request.ChatCompletionRequest;
import org.openaiclient.client.dto.response.ChatCompletionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenAiClient {

	private final RestClient openAiClient;

	public OpenAiClient(
		@Value("${client.openai.url}") String url,
		@Value("${client.openai.key}") String key
	) {
		this.openAiClient = RestClient.builder()
			.baseUrl(url)
			.defaultHeader("Authorization", "Bearer " + key)
			.defaultHeader("Content-Type", "application/json")
			.build();
	}

	public ChatCompletionResponse getChatCompletion(ChatCompletionRequest chatCompletionRequest) {
		return openAiClient.post()
			.body(chatCompletionRequest)
			.retrieve()
			.onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
				throw new RuntimeException("Client error: " + res.getStatusCode());
			})
			.onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
				throw new RuntimeException("Server error: " + res.getStatusCode());
			})
			.body(ChatCompletionResponse.class);
	}

	@Async
	public CompletableFuture<ChatCompletionResponse> getChatCompletionAsync(
		ChatCompletionRequest chatCompletionRequest) {
		return CompletableFuture.supplyAsync(() -> openAiClient.post()
			.body(chatCompletionRequest)
			.retrieve()
			.onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
				throw new RuntimeException("Client error: " + res.getStatusCode());
			})
			.onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
				throw new RuntimeException("Server error: " + res.getStatusCode());
			})
			.body(ChatCompletionResponse.class));
	}
}
