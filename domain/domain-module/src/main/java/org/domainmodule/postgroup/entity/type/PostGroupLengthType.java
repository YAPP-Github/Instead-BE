package org.domainmodule.postgroup.entity.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostGroupLengthType {
	SHORT(140, "짧은 게시물"),
	MEDIUM(300, "보통 게시물"),
	LONG(1000, "긴 게시물");

	private final int maxLength;
	private final String description;
}
