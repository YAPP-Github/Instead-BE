package org.feedclient.client;

import java.time.LocalDateTime;

import org.feedclient.client.rss.RssClient;
import org.feedclient.client.rss.dto.RssResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class RssClientTest {

	@Autowired
	RssClient rssClient;

	@Test
	void getRssFeedTest() {
		// Given
		String feedUrl = "https://rss.app/feeds/v1.1/t3OECiAGSXYOG8h5.json";

		// When
		RssResponse rssFeed = rssClient.getRssFeed(feedUrl);

		// Then
		Assertions.assertEquals(25, rssFeed.getItems().size());
	}

	@Test
	void rssFeedDateTest() {
		// Given
		String feedUrl = "https://rss.app/feeds/v1.1/t3OECiAGSXYOG8h5.json";

		// When
		RssResponse rssFeed = rssClient.getRssFeed(feedUrl);

		// Then
		System.out.println(rssFeed.getItems().get(0).getDatePublished());
		Assertions.assertTrue(
			rssFeed.getItems().get(0).getDatePublished().isAfter(LocalDateTime.of(2025, 1, 16, 2, 0, 0)));
	}
}
