package org.mainapplication.domain.newscategory.controller.response;

import org.domainmodule.rssfeed.entity.RssFeed;
import org.domainmodule.rssfeed.entity.type.FeedCategoryType;

public record NewsCategoryResponse(
	FeedCategoryType category,
	String name
) {

	public static NewsCategoryResponse of(RssFeed rssFeed) {
		return new NewsCategoryResponse(
			rssFeed.getCategory(),
			rssFeed.getCategory().getValue()
		);
	}
}
