package org.openaiclient.client.dto.request.type;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseFormat {

	private String type;

	@JsonProperty("json_schema")
	private Map<String, Object> jsonSchema;

	public ResponseFormat(String type, Map<String, Object> jsonSchema) {
		this.type = type;
		this.jsonSchema = jsonSchema;
	}
}
