package org.mainapp.domain.newscategory.service;

import java.util.List;

import org.domainmodule.rssfeed.entity.RssFeed;
import org.domainmodule.rssfeed.repository.RssFeedRepository;
import org.mainapp.domain.newscategory.controller.response.NewsCategoryResponse;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsCategoryService {

	private final RssFeedRepository rssFeedRepository;

	/**
	 * 전체 RssFeed를 조회하는 메서드
	 */
	public List<NewsCategoryResponse> getNewsCategories() {
		List<RssFeed> rssFeeds = rssFeedRepository.findAllActivated();
		return rssFeeds.stream()
			.map(NewsCategoryResponse::of)
			.toList();
	}
}
