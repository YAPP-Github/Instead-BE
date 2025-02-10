package org.openaiclient.client.dto.response.type;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseMessage {

	private String role;

	private String content;

	private String refusal;
}
