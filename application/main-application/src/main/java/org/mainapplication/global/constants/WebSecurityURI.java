package org.mainapplication.global.constants;

import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebSecurityURI {

	public static final List<String> PUBLIC_URIS = List.of(
		"/swagger-ui/**",
		"/login/oauth2/**",
		"/login",
		"/oauth2/",
		"/api/v1/auth/login/oauth2/code/google"
	);

	public static final List<String> CORS_ALLOW_URIS =
		List.of("http://localhost:3000","http://localhost:8080");
}
