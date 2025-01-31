package org.domainmodule.rssfeed.exception;

import org.domainmodule.rssfeed.entity.type.FeedCategoryType;

public class RssFeedNotFoundException extends RuntimeException {

	public RssFeedNotFoundException(FeedCategoryType feedCategory) {
		super("RSS 피드가 존재하지 않습니다. category: " + feedCategory);
	}
}
