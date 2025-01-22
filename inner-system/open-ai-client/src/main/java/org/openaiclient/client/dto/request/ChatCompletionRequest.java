package org.openaiclient.client.dto.request;

import java.util.List;

import org.openaiclient.client.dto.request.type.RequestMessage;
import org.openaiclient.client.dto.request.type.ResponseFormat;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatCompletionRequest {

	private String model;

	private List<RequestMessage> messages;

	@JsonProperty("response_format")
	private ResponseFormat responseFormat;

	@Nullable
	@JsonProperty("n")
	private Integer number;

	@Nullable
	private Double temperature;

	public ChatCompletionRequest(
		String model,
		List<RequestMessage> messages,
		ResponseFormat responseFormat,
		Integer number,
		Double temperature
	) {
		this.model = model;
		this.messages = messages;
		this.responseFormat = responseFormat;
		this.number = number;
		this.temperature = temperature;
	}
}
