package org.feedclient.client;

import org.feedclient.rss.RssClient;
import org.feedclient.rss.dto.RssResponse;
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
	void getRssFeed() {
		// Given
		String feedUrl = "https://rss.app/feeds/v1.1/tMxpbLaaVTNSJ0nI.json";

		// When
		RssResponse rssFeed = rssClient.getRssFeed(feedUrl);

		// Then
		Assertions.assertEquals(25, rssFeed.getItems().size());
	}
}
