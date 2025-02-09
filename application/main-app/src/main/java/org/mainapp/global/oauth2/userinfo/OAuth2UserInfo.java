package org.mainapp.global.oauth2.userinfo;

import java.util.Map;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class OAuth2UserInfo {

	protected final Map<String, Object> attributes;

	public abstract String getProvider();

	public abstract String getProviderId();

	public abstract String getEmail();

	public abstract String getName();
}
