package org.snsclient.twitter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Getter
@Configuration
public class TwitterConfig {
	@Value("${sns.twitter.challenge}")
	private String challenge;
	
	@Value("${sns.twitter.redirect-url}")
	private String redirectUri;
}