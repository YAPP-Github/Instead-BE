package org.openaiclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OpenAiClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpenAiClientApplication.class, args);
	}

}
