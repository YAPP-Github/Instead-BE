package org.mainapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = {"org.mainapp", "org.domainmodule", "org.feedclient", "org.openaiclient", "org.snsclient"})
@EnableAsync
public class MainApp {

	public static void main(String[] args) {
		System.setProperty("spring.config.name", "application,application-domain,application-sns"); //yml 파일 import
		SpringApplication.run(MainApp.class, args);
	}
}
