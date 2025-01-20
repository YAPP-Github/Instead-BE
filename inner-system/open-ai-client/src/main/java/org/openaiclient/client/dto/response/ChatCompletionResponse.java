package org.openaiclient.client.dto.response;

import java.util.List;

import org.openaiclient.client.dto.response.type.Choice;
import org.openaiclient.client.dto.response.type.Usage;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class ChatCompletionResponse {

	private String id;

	private List<Choice> choices;

	private Integer created;

	private String model;

	@Nullable
	@JsonProperty("service_tier")
	private String serviceTier;

	@JsonProperty("system_fingerprint")
	private String systemFingerprint;

	private String object;

	private Usage usage;
}
