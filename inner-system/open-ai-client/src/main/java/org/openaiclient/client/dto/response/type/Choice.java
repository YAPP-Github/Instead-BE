package org.openaiclient.client.dto.response.type;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Choice {

	private Integer index;

	private ResponseMessage message;

	@JsonProperty("finish_reason")
	private String finishReason;
}
