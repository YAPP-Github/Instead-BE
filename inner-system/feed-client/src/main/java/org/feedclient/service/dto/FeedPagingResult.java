package org.feedclient.service.dto;

import java.util.List;

import org.feedclient.service.type.FeedItem;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FeedPagingResult {

	private final boolean eof;

	private final List<FeedItem> feedItems;

	public static FeedPagingResult of(List<FeedItem> feedItems, boolean eof) {
		return new FeedPagingResult(eof, List.copyOf(feedItems));
	}
}
