package org.openaiclient.client.dto.request.type;

import lombok.Getter;

@Getter
public class RequestMessage {

	private String role;

	private String content;

	public RequestMessage(String role, String content) {
		this.role = role;
		this.content = content;
	}
}
