package org.domainmodule.postgroup.entity.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostGroupPurposeType {
	INFORMATION("정보 전달"),
	OPINION("의견 표출"),
	HUMOR("유머"),
	MARKETING("마케팅/홍보");

	private final String value;
}
