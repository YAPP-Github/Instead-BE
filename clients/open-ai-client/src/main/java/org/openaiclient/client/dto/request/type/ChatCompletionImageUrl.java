package org.openaiclient.client.dto.request.type;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class ChatCompletionImageUrl {

	private String url;

	private ImageDetailType detail;
}
