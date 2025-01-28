package org.domainmodule.rssfeed.entity.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeedCategoryType {
	INVEST("투자"),
	STOCK("주식"),
	REALESTATE("부동산"),
	FASHION("패션"),
	TRAVEL("여행"),
	BEAUTY("뷰티"),
	FITNESS("피트니스"),
	COOKING("요리"),
	HEALTHCARE("헬스케어"),
	AI("AI"),
	GAME("게임"),
	APP("앱"),
	SPACE("우주"),
	ENVIRONMENT("환경"),
	ENGINEER("공학");

	private final String value;
}
