package org.domainmodule.post.entity.type;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UploadTimeType {
	MORNING("오전", LocalTime.of(9, 0), LocalTime.of(11, 0)),
	LUNCH("점심", LocalTime.of(11, 0), LocalTime.of(13, 0)),
	AFTERNOON("오후", LocalTime.of(13, 0), LocalTime.of(17, 0)),
	EVENING("저녁", LocalTime.of(17, 0), LocalTime.of(21, 0)),
	NIGHT("밤", LocalTime.of(21, 0), LocalTime.of(1, 0)),
	RANDOM("전체 선택", null, null); // 무작위 업로드

	private final String label;
	private final LocalTime startTime;
	private final LocalTime endTime;
}
