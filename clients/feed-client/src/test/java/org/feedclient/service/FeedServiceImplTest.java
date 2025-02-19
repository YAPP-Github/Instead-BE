package org.feedclient.service;

import org.feedclient.service.dto.FeedPagingResult;
import org.feedclient.service.type.FeedItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class FeedServiceImplTest {

	@Autowired
	FeedService feedService;

	@Test
	void getPagedFeedTest() {
		// Given
		String feedUrl = "https://rss.app/feeds/v1.1/zHvdVTrJSiWM5qfm.json";

		// When
		FeedPagingResult newsFeed = feedService.getPagedFeed(feedUrl, 5);
		for (FeedItem feedItem : newsFeed.getFeedItems()) {
			System.out.println(feedItem.getId() + ": " + feedItem.getTitle());
			System.out.println(feedItem.getDatePublished());
			System.out.println("=====================================================");
			System.out.println(feedItem.getContent());
			System.out.println();
		}

		// Then
		Assertions.assertAll(
			() -> Assertions.assertEquals(5, newsFeed.getFeedItems().size()),
			() -> Assertions.assertFalse(newsFeed.isEof()),
			() -> Assertions.assertInstanceOf(FeedItem.class, newsFeed.getFeedItems().get(0))
		);
	}

	@Test
	void getPagedFeedByCursorTest() {
		// Given
		String feedUrl = "https://rss.app/feeds/v1.1/zHvdVTrJSiWM5qfm.json";

		// When
		FeedPagingResult newsFeed = feedService.getPagedFeed(feedUrl, "bcc71628d40e410be723c65dc9af409e", 5);
		for (FeedItem feedItem : newsFeed.getFeedItems()) {
			System.out.println(feedItem.getId() + ": " + feedItem.getTitle());
			System.out.println(feedItem.getDatePublished());
			System.out.println("=====================================================");
			System.out.println(feedItem.getContent());
			System.out.println();
		}

		// Then
		Assertions.assertAll(
			() -> Assertions.assertEquals(5, newsFeed.getFeedItems().size()),
			() -> Assertions.assertFalse(newsFeed.isEof()),
			() -> Assertions.assertInstanceOf(FeedItem.class, newsFeed.getFeedItems().get(0))
		);
	}
}
