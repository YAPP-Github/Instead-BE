package org.feedclient.rss;

import org.feedclient.rss.dto.RssResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RssClient {

	private final RestClient rssClient;

	public RssClient(RestClient.Builder clientBuilder) {
		this.rssClient = clientBuilder.build();
	}

	public RssResponse getRssFeed(String feedUrl) {
		return rssClient.get()
			.uri(feedUrl)
			.retrieve()
			.onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
				throw new RuntimeException("Client error: " + res.getStatusCode());
			})
			.onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
				throw new RuntimeException("Server error: " + res.getStatusCode());
			})
			.body(RssResponse.class);
	}
}
