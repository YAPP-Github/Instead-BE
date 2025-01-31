package org.feedclient.service;

import org.feedclient.service.dto.FeedPagingResult;

public interface FeedService {

	FeedPagingResult getPagedFeed(String feedUrl, int limit);

	FeedPagingResult getPagedFeed(String feedUrl, String cursorId, int limit);
}
