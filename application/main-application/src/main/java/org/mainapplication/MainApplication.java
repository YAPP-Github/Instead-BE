package org.mainapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.mainapplication", "org.domainmodule", "org.snsclient"})
public class MainApplication {

	public static void main(String[] args) {
		System.setProperty("spring.config.name", "application,application-domain,application-sns");
		SpringApplication.run(MainApplication.class, args);
	}

}

