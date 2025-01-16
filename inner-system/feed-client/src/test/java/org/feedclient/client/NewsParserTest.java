package org.feedclient.client;

import org.feedclient.newsparser.NewsParserClient;
import org.feedclient.newsparser.dto.NewsParserResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class NewsParserTest {

	@Autowired
	NewsParserClient newsParserClient;

	@Test
	void parseNews200Test() {
		// Given
		String newsUrl = "https://www.arsenal.com/news/every-word-mikels-pre-tottenham-presser-1";

		// When
		NewsParserResponse news = newsParserClient.parseNews(newsUrl);

		// Then
		System.out.println(news.toString());
		Assertions.assertEquals(200, news.getStatusCode());
	}

	@Test
	void parseNews206Test() {
		// Given
		String newsUrl = "https://m.sports.naver.com/wfootball/article/076/0004236244";

		// When
		NewsParserResponse news = newsParserClient.parseNews(newsUrl);

		// Then
		System.out.println(news.toString());
		Assertions.assertEquals(206, news.getStatusCode());
	}

	@Test
	void parseNews400Test() {
		// Given
		String newsUrl = "https://aaaaaaaaa";

		// When
		NewsParserResponse news = newsParserClient.parseNews(newsUrl);

		// Then
		System.out.println(news.toString());
		Assertions.assertEquals(400, news.getStatusCode());
	}
}
