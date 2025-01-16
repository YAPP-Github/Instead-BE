package org.domainmodule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DomainModuleApplication {

	public static void main(String[] args) {
		SpringApplication.run(DomainModuleApplication.class, args);
	}

}
