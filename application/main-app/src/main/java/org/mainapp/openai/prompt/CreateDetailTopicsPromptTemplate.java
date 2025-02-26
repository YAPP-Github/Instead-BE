package org.mainapp.openai.prompt;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class CreateDetailTopicsPromptTemplate {

	public String getGenerateDetailTopicsPrompt(String topic, Integer n) {
		return topic + "이라는 주제와 관련된 중복되지 않는 세부 주제를 " + n + "개 추천해줘. 관련된 다른 주제여도 좋아.";
	}

	public String getExcludeExistTopicsPrompt(List<String> existTopics) {
		if (existTopics == null || existTopics.isEmpty()) {
			return null;
		}

		List<String> existTopicsPrompt = existTopics.stream()
			.map(existTopic -> existTopic + "\n")
			.toList();

		return "세부 주제를 추천할 때, 다음 주제를과 겹치지 않는 내용으로 추천해줘.\n" + existTopicsPrompt;
	}
}
