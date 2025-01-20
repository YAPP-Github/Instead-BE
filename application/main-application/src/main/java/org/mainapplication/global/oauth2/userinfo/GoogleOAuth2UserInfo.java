package org.mainapplication.global.oauth2.userinfo;

import java.util.Map;

import org.domainmodule.user.entity.type.ProviderType;

public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

	public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
		super(attributes);
	}

	@Override
	public String getProvider() {
		return ProviderType.GOOGLE.getValue();
	}

	@Override
	public String getProviderId() {
		return (String) attributes.get("sub");
	}

	@Override
	public String getEmail() {
		return (String) attributes.get("email");
	}

	@Override
	public String getName() {
		return (String) attributes.get("name");
	}

	public static GoogleOAuth2UserInfo fromAttributes(Map<String, Object> attributes) {
		return new GoogleOAuth2UserInfo(attributes);
	}
}
