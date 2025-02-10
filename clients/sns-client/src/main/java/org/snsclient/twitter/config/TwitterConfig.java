package org.snsclient.twitter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import twitter4j.OAuth2TokenProvider;
import twitter4j.conf.ConfigurationBuilder;

@Getter
@Configuration
public class TwitterConfig {
	@Value("${sns.twitter.client-id}")
	private String clientId;

	@Value("${sns.twitter.client-secret}")
	private String clientSecret;

	@Value("${sns.twitter.challenge}")
	private String challenge;
	
	@Value("${sns.twitter.redirect-url}")
	private String redirectUri;

	/**
	 * twitter4j의 OAuth2TokenProvider
	 * twitter API 사용에 필요한 access token 발급받을 때 사용
	 */
	@Bean
	public OAuth2TokenProvider twitterOAuth2TokenProvider() {
		twitter4j.conf.Configuration configuration = new ConfigurationBuilder()
			.setOAuthConsumerKey(clientId)
			.setOAuthConsumerSecret(clientSecret)
			.build();
		return new OAuth2TokenProvider(configuration);
	}
}