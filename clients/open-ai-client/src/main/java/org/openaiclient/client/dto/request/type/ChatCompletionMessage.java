package org.openaiclient.client.dto.request.type;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatCompletionMessage {

	private RoleType role;

	private List<ChatCompletionContent> content;
}
