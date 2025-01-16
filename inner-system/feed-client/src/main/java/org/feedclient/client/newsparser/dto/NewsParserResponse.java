package org.feedclient.client.newsparser.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsParserResponse {

	private Integer statusCode;

	private String message;

	private String body;

	@Override
	public String toString() {
		return "statusCode: " + statusCode + "\n"
			+ "message: " + message + "\n"
			+ "body: " + body + "\n";
	}
}
