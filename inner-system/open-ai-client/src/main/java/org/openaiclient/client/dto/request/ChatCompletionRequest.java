package org.openaiclient.client.dto.request;

import java.util.List;

import org.openaiclient.client.dto.request.type.RequestMessage;
import org.openaiclient.client.dto.request.type.ResponseFormat;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatCompletionRequest {

	private String model;

	private List<RequestMessage> messages;

	@JsonProperty("response_format")
	private ResponseFormat responseFormat;

	public ChatCompletionRequest(String model, List<RequestMessage> messages, ResponseFormat responseFormat) {
		this.model = model;
		this.messages = messages;
		this.responseFormat = responseFormat;
	}
}
