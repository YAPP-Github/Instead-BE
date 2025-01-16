package org.feedclient.client.rss;

import org.feedclient.client.rss.dto.RssResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RssClient {

	private final RestClient rssClient;

	public RssClient(RestClient.Builder clientBuilder) {
		this.rssClient = clientBuilder.build();
	}

	/**
	 * RSS 피드에서 뉴스 목록 전체 조회
	 */
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
