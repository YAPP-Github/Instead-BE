package org.feedclient.client.rss.type;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
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
	private LocalDateTime datePublished;
}
