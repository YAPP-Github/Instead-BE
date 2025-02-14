package org.feedclient.service.type;

import java.time.LocalDateTime;

import org.feedclient.client.rss.type.RssItem;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedItem {

	private String id;

	private String url;

	private String title;

	private String contentSummary;

	private String content;

	private String image;

	private LocalDateTime datePublished;

	public static FeedItem of(RssItem rssItem, String content) {
		FeedItem feedItem = new FeedItem();
		feedItem.id = rssItem.getId();
		feedItem.url = rssItem.getUrl();
		feedItem.title = rssItem.getTitle();
		feedItem.image = rssItem.getImage();
		feedItem.datePublished = rssItem.getDatePublished();
		feedItem.contentSummary = rssItem.getContentText();
		feedItem.content = content;
		return feedItem;
	}
}
