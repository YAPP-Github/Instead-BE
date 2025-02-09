package org.mainapp.openai.contentformat.jsonschema;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.openaiclient.client.dto.request.type.ResponseFormat;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

@Getter
@Component
public class SummaryContentSchema implements ResponseSchema {

	private final Map<String, Object> schema;

	public SummaryContentSchema() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		ClassPathResource resource = new ClassPathResource("jsonschema/summary-content-response.json");
		try (InputStream inputStream = resource.getInputStream()) {
			this.schema = objectMapper.readValue(inputStream, new TypeReference<Map<String, Object>>() {
			});
		}
	}

	public ResponseFormat getResponseFormat() {
		return new ResponseFormat("json_schema", schema);
	}
}
