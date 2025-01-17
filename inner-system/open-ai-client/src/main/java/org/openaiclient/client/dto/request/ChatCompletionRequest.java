package org.openaiclient.client.dto.request;

import java.util.List;

import org.openaiclient.client.dto.request.type.RequestMessage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatCompletionRequest {

	private String model;

	private List<RequestMessage> messages;

	public ChatCompletionRequest(String model, List<RequestMessage> messages) {
		this.model = model;
		this.messages = messages;
	}
}
