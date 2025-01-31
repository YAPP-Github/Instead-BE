package org.domainmodule.user.entity.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProviderType {
	GOOGLE("google"),
	KAKAO("kakao"),
	NAVER("naver");

	private final String value;

	public static ProviderType fromValue(String value) {
		for (ProviderType type : ProviderType.values()) {
			if (type.value.equalsIgnoreCase(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("지원되지 않는 Provider 입니다.");
	}
}