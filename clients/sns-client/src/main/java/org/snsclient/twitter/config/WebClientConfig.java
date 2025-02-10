package org.snsclient.twitter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	//TODO S3에서 이미지를 다운해서 multipart로 다시 넣어서 5MB로 늘려줬는데 괜찮을지
	@Bean
	public WebClient webClient() {
		return WebClient.builder()
			.defaultHeader("Content-Type", "multipart/form-data")
			.codecs(configurer ->
				configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024) // 5MB로 증가
			)
			.build();
	}
}
