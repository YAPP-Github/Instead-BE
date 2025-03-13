package org.mainapp.domain.post.controller.request;

import java.time.LocalDate;

import org.domainmodule.post.entity.type.UploadTimeType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "게시물 업로드 시간 예약하기 요청 본문")
public record ReserveUploadTimeRequest(
	@Schema(description = "하루에 업로드 할 글의 수", example = "2")
	@NotNull(message = "업로드 할 글의 수를 지정해주세요.")
	int dailyUploadCount,
	@Schema(description = "업로드 시작 시간", example = "2025-01-01")
	@NotNull(message = "업로드 시작 날짜을 지정해주세요.")
	LocalDate uploadStartDate,
	@Schema(description = "업로드 할 시간대", example = "MORNING")
	@NotNull(message = "업로드 할 시간대를 지정해주세요.")
	UploadTimeType uploadTimeType
) {
}
