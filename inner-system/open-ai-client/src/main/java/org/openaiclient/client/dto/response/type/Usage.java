package org.openaiclient.client.dto.response.type;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Usage {

	@JsonProperty("prompt_tokens")
	private Integer promptTokens;

	@JsonProperty("completion_tokens")
	private Integer completionTokens;

	@JsonProperty("total_tokens")
	private Integer totalTokens;
}
