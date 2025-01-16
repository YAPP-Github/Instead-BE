package org.feedclient.client.rss.dto;

import java.util.List;

import org.feedclient.client.rss.type.RssItem;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RssResponse {

	private String version;

	private String title;

	@JsonProperty("home_page_url")
	private String homePageUrl;

	@JsonProperty("feed_url")
	private String feedUrl;

	private String language;

	private String description;

	private List<RssItem> items;

	@Override
	public String toString() {
		return version + "\n"
			+ title + "\n"
			+ homePageUrl + "\n"
			+ feedUrl + "\n"
			+ language + "\n"
			+ description + "\n";
	}
}
