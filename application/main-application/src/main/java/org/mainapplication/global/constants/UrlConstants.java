package org.mainapplication.global.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UrlConstants {
	LOCAL_SERVER_URL("url.local.server"),
	LOCAL_DOMAIN_URL("url.local.domain"),
	LOCAL_SECURE_DOMAIN_URL("url.local.secure"),
	BASE_URI("url.base.uri");
	private String value;
}
