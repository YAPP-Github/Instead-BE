package org.mainapplication.domain.post.controller.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.domainmodule.post.entity.PromptHistory;
import org.domainmodule.post.entity.type.PostPromptType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시물 프롬프트 내역 응답 객체")
public record PromptHistoriesResponse(
	@Schema(description = "게시물 프롬프트 내역 id", example = "1")
	long id,
	@Schema(description = "게시물 프롬프트 내역 생성 일시", example = "2025-01-01T00:00:00.000Z")
	LocalDateTime createdAt,
	@Schema(description = "프롬프트 내용", example = "암튼 해줭")
	String prompt,
	@Schema(description = "프롬프트에 대한 답변 (게시물 본문)", example = "해드렸습니다")
	String response,
	@Schema(description = "게시물 프롬프트 적용 타입에 대한 Enum (개별/전체)", example = "EACH")
	PostPromptType type
) {
	public static PromptHistoriesResponse of(long id, LocalDateTime createdAt, String prompt, String response,
		PostPromptType promptType) {
		return new PromptHistoriesResponse(id, createdAt, prompt, response, promptType);
	}

	public static PromptHistoriesResponse from(PromptHistory history) {
		return new PromptHistoriesResponse(
			history.getId(),
			history.getCreatedAt(),
			history.getPrompt(),
			history.getResponse(),
			history.getPromptType()
		);
	}

	public static List<PromptHistoriesResponse> fromList(List<PromptHistory> histories) {
		return histories.stream()
			.map(PromptHistoriesResponse::from)
			.collect(Collectors.toList());
	}
}

