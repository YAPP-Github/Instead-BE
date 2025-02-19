package org.mainapp.global.constants;

import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebSecurityURI {

	public static final List<String> PUBLIC_URIS = List.of(
		"/swagger-ui/**",
		"/v3/api-docs/**",
		"/swagger-resources/**",
		"/common/health/**",
		"/twitter/success/**"
	);

	public static final List<String> CORS_ALLOW_URIS =
		List.of(UrlConstants.LOCAL_DOMAIN_URL, UrlConstants.PROD_DOMAIN_URL, UrlConstants.SERVER_DOMAIN_URL,
			UrlConstants.DEV_SERVER_DOMAIN_URL);
}
