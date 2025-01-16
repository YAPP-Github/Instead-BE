package org.feedclient.service.dto;

import java.util.List;

import org.feedclient.service.type.FeedItem;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedPagingResult {

	private int size;

	private boolean eof;

	private List<FeedItem> feedItems;

	public static FeedPagingResult of(List<FeedItem> feedItems, int size, boolean eof) {
		FeedPagingResult feedPagingResult = new FeedPagingResult();
		feedPagingResult.feedItems = feedItems;
		feedPagingResult.size = size;
		feedPagingResult.eof = eof;
		return feedPagingResult;
	}
}
