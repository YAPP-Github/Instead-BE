package org.scheduleapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.scheduleapp", "org.domainmodule", "org.snsclient"})
public class ScheduleApp {
	public static void main(String[] args) {
		System.setProperty("spring.config.name", "application,application-domain,application-sns");
		SpringApplication.run(ScheduleApp.class, args);
	}
}
