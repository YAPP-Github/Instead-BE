package org.security.dto;

import java.util.Map;

import org.commonmodule.dto.OAuth2Response;
import org.domainmodule.user.entity.type.ProviderType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GoogleResponse implements OAuth2Response {
	private final Map<String, Object> attribute;

	@Override
	public String getProvider() {
		return ProviderType.GOOGLE.getValue();
	}

	@Override
	public String getProviderId() {
		return attribute.get("sub").toString();
	}

	@Override
	public String getEmail() {
		return attribute.get("email").toString();
	}

	@Override
	public String getName() {
		return attribute.get("name").toString();
	}

	public static GoogleResponse fromAttributes(Map<String, Object> attributes) {
		return new GoogleResponse(attributes);
	}
}
