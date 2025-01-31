package org.mainapplication.domain.post.service;

import java.util.List;

import org.domainmodule.post.entity.PromptHistory;
import org.domainmodule.post.repository.PromptHistoryRepository;
import org.mainapplication.domain.post.controller.response.PromptHistoriesRespone;
import org.mainapplication.domain.post.exception.PostErrorCode;
import org.mainapplication.global.error.CustomException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromptHistoryService {
	private final PromptHistoryRepository promptHistoryRepository;

	/**
	 * 과거 프롬프트 내역 가져오기
	 * @return
	 */
	public List<PromptHistoriesRespone> getPromptHistories(Long agentId, Long postGroupId, Long postId) {
		//TODO 임시 설정한 부분 (이후 securityContext에서 userId가져오기)
		long userId = 1L;

		List<PromptHistory> histories = promptHistoryRepository.findPromptHistoriesWithValidation(userId, agentId, postGroupId, postId);

		if (histories.isEmpty()) {
			throw new CustomException(PostErrorCode.PROMPT_HISTORIES_NOT_FOUND);
		}

		return PromptHistoriesRespone.fromList(histories);
	}
}
