package org.modulesns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.modulesns", "org.modulecommon"})
public class ModuleSnsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModuleSnsApplication.class, args);
	}

}
