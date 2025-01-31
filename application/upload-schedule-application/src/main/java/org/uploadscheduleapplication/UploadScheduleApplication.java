package org.uploadscheduleapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.uploadscheduleapplication", "org.domainmodule", "org.snsclient"})
public class UploadScheduleApplication {
	public static void main(String[] args) {
		System.setProperty("spring.config.name", "application,application-domain,application-sns");
		SpringApplication.run(UploadScheduleApplication.class, args);
	}
}
