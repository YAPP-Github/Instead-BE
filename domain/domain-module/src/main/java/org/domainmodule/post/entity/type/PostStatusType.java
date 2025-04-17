package org.domainmodule.post.entity.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostStatusType {
	GENERATED("생성 됨"),
	EDITING("수정 중"),
	READY_TO_UPLOAD("업로드 준비 완료"),
	UPLOAD_RESERVED("업로드 대기"),
	UPLOAD_CONFIRMED("업로드 확정"),
	UPLOADED("업로드 완료"),
	UPLOAD_FAILED("업로드 실패");
	private final String value;
}
