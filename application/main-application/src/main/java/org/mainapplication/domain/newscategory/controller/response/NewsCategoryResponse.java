package org.mainapplication.domain.newscategory.controller.response;

import org.domainmodule.rssfeed.entity.RssFeed;
import org.domainmodule.rssfeed.entity.type.FeedCategoryType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "뉴스 카테고리 목록 조회 API 응답 본문")
public record NewsCategoryResponse(
	@Schema(description = "코드 레벨에서 카테고리를 구분하는 Enum", example = "INVEST")
	FeedCategoryType category,
	@Schema(description = "화면에 표시될 카테고리 한글명", example = "투자")
	String name
) {

	public static NewsCategoryResponse of(RssFeed rssFeed) {
		return new NewsCategoryResponse(
			rssFeed.getCategory(),
			rssFeed.getCategory().getValue()
		);
	}
}
