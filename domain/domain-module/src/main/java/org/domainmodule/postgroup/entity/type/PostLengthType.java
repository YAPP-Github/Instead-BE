package org.domainmodule.postgroup.entity.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostLengthType {
	SHORT(200, "짧은 게시물"),
	MEDIUM(400, "보통 게시물"),
	LONG(600, "긴 게시물");

	private final int maxLength;
	private final String description;
}
