package org.mainapp.domain.post.service;

import java.util.List;

import org.domainmodule.post.entity.Post;
import org.domainmodule.post.entity.PromptHistory;
import org.domainmodule.post.entity.type.PostPromptType;
import org.domainmodule.post.repository.PromptHistoryRepository;
import org.mainapp.domain.post.controller.response.PromptHistoriesResponse;
import org.mainapp.global.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostPromptHistoryService {
	private final PromptHistoryRepository promptHistoryRepository;

	/**
	 * 과거 프롬프트 내역 가져오기
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<PromptHistoriesResponse> getPromptHistories(Long agentId, Long postGroupId, Long postId) {
		final long userId = Long.parseLong(SecurityUtil.getCurrentMemberId());

		List<PromptHistory> histories = promptHistoryRepository.findPromptHistoriesWithValidation(userId, agentId,
			postGroupId, postId);

		return PromptHistoriesResponse.fromList(histories);
	}

	@Transactional
	public PromptHistory createPromptHistories(Post post, String prompt, String content, PostPromptType type) {
		PromptHistory promptHistory = PromptHistory.createPromptHistory(post, prompt, content, type);
		return promptHistoryRepository.save(promptHistory);
	}
}
