package org.feedclient.client.newsparser;

import java.util.concurrent.CompletableFuture;

import org.feedclient.client.newsparser.dto.NewsParserRequest;
import org.feedclient.client.newsparser.dto.NewsParserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NewsParserClient {

	private final RestClient newsParserClient;

	public NewsParserClient(
		@Value("${client.news-parser.url}") String newsParserUrl,
		RestClient.Builder clientBuilder
	) {
		this.newsParserClient = clientBuilder.baseUrl(newsParserUrl).build();
	}

	public NewsParserResponse parseNews(String newsUrl) {
		return newsParserClient.post()
			.body(new NewsParserRequest(newsUrl))
			.retrieve()
			.onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
				throw new RuntimeException("Client error: " + res.getStatusCode());
			})
			.onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
				throw new RuntimeException("Server error: " + res.getStatusCode());
			})
			.body(NewsParserResponse.class);
	}

	@Async
	public CompletableFuture<NewsParserResponse> parseNewsAsync(String newsUrl) {
		return CompletableFuture.supplyAsync(() -> parseNews(newsUrl));
	}
}
