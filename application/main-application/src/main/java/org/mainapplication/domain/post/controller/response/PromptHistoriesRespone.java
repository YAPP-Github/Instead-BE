package org.mainapplication.domain.post.controller.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.domainmodule.post.entity.PromptHistory;
import org.domainmodule.post.entity.type.PostPromptType;

public record PromptHistoriesRespone(
	long id,
	LocalDateTime createdAt,
	String prompt,
	String response,
	PostPromptType type
) {
	public static PromptHistoriesRespone of(long id, LocalDateTime createdAt, String prompt, String response, PostPromptType promptType) {
		return new PromptHistoriesRespone(id, createdAt, prompt, response, promptType);
	}

	public static PromptHistoriesRespone from(PromptHistory history) {
		return new PromptHistoriesRespone(
			history.getId(),
			history.getCreatedAt(),
			history.getPrompt(),
			history.getResponse(),
			history.getPromptType()
		);
	}

	public static List<PromptHistoriesRespone> fromList(List<PromptHistory> histories) {
		return histories.stream()
			.map(PromptHistoriesRespone::from)
			.collect(Collectors.toList());
	}
}

