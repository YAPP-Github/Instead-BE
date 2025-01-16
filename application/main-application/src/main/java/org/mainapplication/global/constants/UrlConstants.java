package org.mainapplication.global.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UrlConstants {
	LOCAL_SERVER_URL("http://localhost:8080"),
	LOCAL_DOMAIN_URL("http://localhost:3000"),
	LOCAL_SECURE_DOMAIN_URL("https://localhost:3000");

	private String value;
}
