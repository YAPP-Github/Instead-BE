package org.openaiclient.client.dto.request.type;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestMessage {

	private String role;

	private String content;

	public RequestMessage(String role, String content) {
		this.role = role;
		this.content = content;
	}
}
