package org.domainmodule.rssfeed.entity.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeedCategoryType {
	INVEST("투자"), // 제거
	STOCK("주식"), // 제거
	REALESTATE("부동산"),
	FASHION("패션"),
	TRAVEL("여행"),
	BEAUTY("뷰티"), // 제거
	FITNESS("피트니스"), // 제거
	COOKING("요리"), // 제거
	HEALTHCARE("헬스케어"),
	AI("AI"),
	GAME("게임"), // 제거
	APP("앱"), // 제거
	SPACE("우주"),
	ENVIRONMENT("환경"),
	ENGINEER("공학"),
	WEATHER("날씨"), // 추가
	FOOD("음식"), // 추가
	EDUCATION("교육"), // 추가
	ACCIDENT("사건사고"), // 추가
	STOCK_DOMESTIC("주식 (국내 뉴스)"), // 추가
	STOCK_GLOBAL("주식 (해외 뉴스)"), // 추가
	FINANCE("금융"); // 추가

	private final String value;
}
