package org.mainapp.openai.contentformat.jsonschema;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SummaryContentSchemaTest {

	@Test
	void getSchema() throws IOException {
		// Given
		SummaryContentSchema summaryContentSchema = new SummaryContentSchema();

		// When
		Map<String, Object> schema = summaryContentSchema.getSchema();

		// Then
		System.out.println(schema.toString());
		Assertions.assertInstanceOf(Map.class, schema);
	}
}
