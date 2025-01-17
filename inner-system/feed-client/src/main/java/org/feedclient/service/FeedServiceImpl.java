package org.feedclient.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.feedclient.client.newsparser.NewsParserClient;
import org.feedclient.client.rss.RssClient;
import org.feedclient.client.rss.type.RssItem;
import org.feedclient.service.dto.FeedPagingResult;
import org.feedclient.service.type.FeedItem;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

	private final RssClient rssClient;
	private final NewsParserClient newsParserClient;

	/**
	 * RSS url로부터, 본문이 포함된 피드 목록을 limit만큼 조회
	 */
	@Override
	public FeedPagingResult getPagedFeed(String feedUrl, int limit) {
		// rss client로부터 rss item 목록 받아오기
		List<RssItem> rssItems = rssClient.getRssFeed(feedUrl).getItems();

		// 페이지네이션 수행
		List<RssItem> pagedRssItems = rssItems.stream()
			.sorted(Comparator.comparing(RssItem::getDatePublished))
			.limit(limit)
			.toList();

		// 본문 파싱 수행
		List<FeedItem> pagedFeedItems = parseRssItems(pagedRssItems);

		// 결과 반환
		return FeedPagingResult.of(pagedFeedItems, pagedFeedItems.size(), false);
	}

	/**
	 * RSS url로부터, 본문이 포함된 피드 목록을 cursorId 이후로 limit만큼 조회
	 */
	@Override
	public FeedPagingResult getPagedFeed(String feedUrl, String cursorId, int limit) {
		// rss client로부터 rss item 목록 받아오기
		List<RssItem> rssItems = rssClient.getRssFeed(feedUrl).getItems();

		// 페이지네이션 수행
		AtomicBoolean eof = new AtomicBoolean(false); // lambda 내부에서 변경하기 위해 AtomicBoolean으로 선언
		List<RssItem> pagedRssItems = getPagedRssItems(rssItems, cursorId, limit, eof);

		// 본문 파싱 수행
		List<FeedItem> pagedFeedItems = parseRssItems(pagedRssItems);

		// 결과 반환
		return FeedPagingResult.of(pagedFeedItems, pagedFeedItems.size(), eof.get());
	}

	/**
	 * cursor를 기준으로 RSS 피드에 pagination을 수행
	 * 1. 현재 피드에 cursor 존재 -> 날짜순으로 정렬한 뒤 cursor 다음 요소부터 limit만큼 반환, 끝에 도달한 경우 eof true로 반환
	 * 2. 현재 피드에 cursor 존재X (피드가 갱신된 경우) -> 날짜순으로 정렬한 뒤 첫 요소부터 limit만큼 반환
	 */
	private static List<RssItem> getPagedRssItems(
		List<RssItem> rssItems,
		String cursorId,
		int limit,
		AtomicBoolean eof
	) {
		return rssItems.stream()
			.sorted(Comparator.comparing(RssItem::getDatePublished))
			.collect(Collectors.collectingAndThen(Collectors.toList(), sortedList -> {
				Optional<RssItem> cursorItem = sortedList.stream()
					.filter(item -> cursorId.equals(item.getId()))
					.findFirst();
				if (cursorItem.isPresent()) {
					int cursorIndex = sortedList.indexOf(cursorItem.get());
					if (sortedList.size() <= cursorIndex + limit + 1) {
						eof.set(true);
						return sortedList.subList(cursorIndex + 1, sortedList.size());
					} else {
						return sortedList.subList(cursorIndex + 1, cursorIndex + 1 + limit);
					}
				} else {
					return sortedList.stream().limit(limit).collect(Collectors.toList());
				}
			}));
	}

	/**
	 * RSS 피드에서 읽어온 그대로의 상태인 RssItem을 받아, 본문을 파싱한 뒤 FeedItem으로 변환
	 */
	private List<FeedItem> parseRssItems(List<RssItem> rssItems) {
		// RssItem 요소별로 비동기로 작업 수행 - url로 본문 파싱한 뒤 FeedItem으로 변환
		List<CompletableFuture<FeedItem>> results = rssItems.stream()
			.map(item -> newsParserClient.parseNewsAsync(item.getUrl())
				.thenApply(result -> FeedItem.of(item, result.getBody())))
			.toList();

		// 각각의 비동기 작업 완료되면 반환
		return results.stream()
			.map(CompletableFuture::join)
			.toList();
	}
}
