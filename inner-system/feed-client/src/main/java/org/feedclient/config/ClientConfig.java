package org.feedclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {

	@Bean
	public RestClient.Builder clientBuilder() {
		return RestClient.builder();
	}
}
