package org.openaiclient.client.dto.request;

import java.util.ArrayList;
import java.util.List;

import org.openaiclient.client.dto.request.type.ChatCompletionContent;
import org.openaiclient.client.dto.request.type.ChatCompletionImageUrl;
import org.openaiclient.client.dto.request.type.ChatCompletionMessage;
import org.openaiclient.client.dto.request.type.ContentType;
import org.openaiclient.client.dto.request.type.ImageDetailType;
import org.openaiclient.client.dto.request.type.ResponseFormat;
import org.openaiclient.client.dto.request.type.RoleType;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatCompletionRequest {

	private String model;

	private List<ChatCompletionMessage> messages = new ArrayList<>();

	@JsonProperty("response_format")
	private ResponseFormat responseFormat;

	@Nullable
	@JsonProperty("n")
	private Integer number;

	@Nullable
	private Double temperature;

	public ChatCompletionRequest(
		String model,
		ResponseFormat responseFormat,
		Integer number,
		Double temperature
	) {
		this.model = model;
		this.responseFormat = responseFormat;
		this.number = number;
		this.temperature = temperature;
	}

	public ChatCompletionRequest addDeveloperMessage(String message) {
		messages.add(
			new ChatCompletionMessage(
				RoleType.developer,
				List.of(new ChatCompletionContent(ContentType.text, message, null))
			)
		);
		return this;
	}

	public ChatCompletionRequest addUserTextMessage(String message) {
		messages.add(
			new ChatCompletionMessage(
				RoleType.user,
				List.of(new ChatCompletionContent(ContentType.text, message, null))
			)
		);
		return this;
	}

	public ChatCompletionRequest addUserImageMessage(String message, List<String> imageUrls) {
		ArrayList<ChatCompletionContent> contents = new ArrayList<>();
		contents.add(new ChatCompletionContent(ContentType.text, message, null));
		imageUrls.stream()
			.map(imageUrl -> new ChatCompletionContent(
				ContentType.image_url,
				null,
				// new ChatCompletionImageUrl("data:image/jpeg;base64," + imageUrl, ImageDetailType.auto))
				new ChatCompletionImageUrl(imageUrl, ImageDetailType.auto))
			)
			.forEach(contents::add);

		messages.add(
			new ChatCompletionMessage(
				RoleType.user,
				contents
			)
		);
		return this;
	}

	public ChatCompletionRequest addAssistantMessage(String message) {
		messages.add(
			new ChatCompletionMessage(
				RoleType.assistant,
				List.of(new ChatCompletionContent(ContentType.text, message, null))
			)
		);
		return this;
	}
}
