package org.domainmodule.rssfeed.entity.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeedCategoryType {
	STOCK("주식"),
	REALESTATE("부동산"),
	WEATHER("날씨"), // 추가
	TRAVEL("여행"),
	HEALTHCARE("건강"),
	FOOD("음식"), // 추가
	EDUCATION("교육"), // 추가
	ACCIDENT("사건사고"), // 추가
	AI("AI"),
	GAME("게임"),
	BASEBALL_KOREA("국내 야구"), // 추가
	BASEBALL_GLOBAL("해외 야구"), // 추가
	FOOTBALL_GLOBAL("해외 축구"), // 추가
	GOLF("골프"), // 추가
	ESPORTS("E-스포츠"), // 추가
	FASHION("패션"),
	BEAUTY("뷰티"),
	INVEST("투자"),
	FITNESS("피트니스"),
	COOKING("요리"),
	APP("앱"),
	SPACE("우주"),
	ENVIRONMENT("환경"),
	ENGINEER("공학");

	private final String value;
}
