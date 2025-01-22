package org.domainmodule.rssfeed.entity.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeedCategoryType {
	COIN("코인"),
	MONEY("돈");

	private final String value;
}
