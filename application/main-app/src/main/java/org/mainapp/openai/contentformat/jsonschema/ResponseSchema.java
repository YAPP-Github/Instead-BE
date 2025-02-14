package org.mainapp.openai.contentformat.jsonschema;

import java.util.Map;

import org.openaiclient.client.dto.request.type.ResponseFormat;

public interface ResponseSchema {

	public Map<String, Object> getSchema();

	public ResponseFormat getResponseFormat();
}
