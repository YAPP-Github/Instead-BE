package org.mainapplication.global.constants;

import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebSecurityURI {

	public static final List<String> PUBLIC_URIS = List.of(
		 UrlConstants.BASE_URI + "/auth/login/oauth2/code/google"
	);

	public static final List<String> CORS_ALLOW_URIS =
		List.of(UrlConstants.LOCAL_DOMAIN_URL, UrlConstants.PROD_DOMAIN_URL);
}
