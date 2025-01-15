package org.feedclient.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RssItem {

	private String id;

	private String url;

	private String title;

	@JsonProperty("content_text")
	private String contentText;

	@JsonProperty("content_html")
	private String contentHtml;

	private String image;

	@JsonProperty("date_published")
	private String datePublished;
}
