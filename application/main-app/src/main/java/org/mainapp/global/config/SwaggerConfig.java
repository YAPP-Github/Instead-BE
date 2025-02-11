package org.mainapp.global.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletContext;

@Configuration
public class SwaggerConfig {
	private static final String PROJECT_NAME = "Instead";
	private static final String SERVER_NAME = PROJECT_NAME + " Server";
	private static final String API_TITLE = PROJECT_NAME + " 서버 API 문서";
	private static final String API_DESCRIPTION = PROJECT_NAME + " 서버 API 문서입니다";
	private static final String GITHUB_URL = "https://github.com/YAPP-Github/25th-Web-Team-1-BE";

	@Value("${swagger.version}")
	private String version;

	@Bean
	public OpenAPI openAPI(ServletContext servletContext) {
		Server server = new Server().url(servletContext.getContextPath()).description(API_DESCRIPTION);
		return new OpenAPI()
			.servers(List.of(server))
			.addSecurityItem(cookieSecurityRequirement())
			.components(authSetting())
			.info(swaggerInfo());
	}

	private Components authSetting() {
		return new Components()
			.addSecuritySchemes(
				"accessToken",
				new SecurityScheme()
					.type(SecurityScheme.Type.APIKEY)
					.in(SecurityScheme.In.COOKIE)
					.bearerFormat("JWT")
					.name("AccessToken")
			)
			.addSecuritySchemes(
				"refreshToken",
				new SecurityScheme()
					.type(SecurityScheme.Type.APIKEY)
					.in(SecurityScheme.In.COOKIE)
					.bearerFormat("JWT")
					.name("RefreshToken")
			);
	}

	private Info swaggerInfo() {
		License license = new License();
		license.setUrl(GITHUB_URL);
		license.setName(SERVER_NAME);

		return new Info()
			.version("v" + version)
			.title(API_TITLE)
			.description(API_DESCRIPTION)
			.license(license);
	}

	private SecurityRequirement cookieSecurityRequirement() {
		SecurityRequirement securityRequirement = new SecurityRequirement();
		securityRequirement.addList("accessToken");
		securityRequirement.addList("refreshToken");
		return securityRequirement;
	}
}
