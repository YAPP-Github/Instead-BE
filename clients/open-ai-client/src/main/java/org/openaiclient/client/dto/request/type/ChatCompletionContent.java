package org.openaiclient.client.dto.request.type;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class ChatCompletionContent {

	private ContentType type;

	private String text;

	@JsonProperty("image_url")
	private ChatCompletionImageUrl imageUrl;
}
