package org.mainapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = {"org.mainapplication", "org.domainmodule", "org.feedclient", "org.openaiclient"})
@EnableAsync
public class MainApplication {

	public static void main(String[] args) {
		System.setProperty("spring.config.name", "application,application-domain,application-sns");
		SpringApplication.run(MainApplication.class, args);
	}

}

