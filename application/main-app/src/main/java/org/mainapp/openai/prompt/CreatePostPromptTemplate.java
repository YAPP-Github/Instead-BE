package org.mainapp.openai.prompt;

import org.domainmodule.agent.entity.type.AgentToneType;
import org.domainmodule.postgroup.entity.type.PostGroupLengthType;
import org.domainmodule.postgroup.entity.type.PostGroupPurposeType;
import org.springframework.stereotype.Component;

@Component
public class CreatePostPromptTemplate {

	/**
	 * 계정 기본 정보 Instruction.
	 * 에이전트의 정보를 받아서 기본 정보에 대한 Instruction을 생성
	 */
	public String getInstructionPrompt(
		String domain,
		String introduction,
		AgentToneType tone,
		String customTone
	) {
		StringBuilder prompt = new StringBuilder();
		prompt.append("너는 SNS 계정을 관리하는 역할이고, 지금부터 소개해줄 계정의 컨셉에 맞게 게시물을 생성해야 해.\n");

		// 계정 소개 설정
		if (introduction != null) {
			prompt.append("계정의 전체적인 소개를 해줄게. ").append(introduction).append("\n");
		}

		// 계정 분야 설정
		if (domain != null) {
			prompt.append("계정이 다룰 분야는 다음과 같아. ").append(domain).append("\n");
		}

		// 계정 말투 설정
		if (tone != null && tone != AgentToneType.CUSTOM) {
			prompt
				.append("게시물을 생성할 때 말투는 ")
				.append(tone.getSuffix()).append("와 같이 ")
				.append(tone.getValue()).append("을 사용해야 해.\n");
		} else if (customTone != null && tone == AgentToneType.CUSTOM) {
			prompt
				.append("게시물을 생성할 때 말투는 다음과 같이 사용해야 해. ")
				.append(customTone).append("\n");
		}

		prompt.append("여기까지 너가 관리해야 할 계정에 대한 설정이야. 이 설정을 바탕으로 게시물 내용을 생성해야 해.\n\n");
		return prompt.toString();
	}

	/**
	 * 게시물 그룹에 설정된 주제 Prompt.
	 * 게시물 그룹의 정보를 받아서 게시물 주제에 대한 Prompt를 생성
	 */
	public String getTopicPrompt(
		String topic,
		PostGroupPurposeType purpose,
		PostGroupLengthType length,
		String content
	) {
		StringBuilder prompt = new StringBuilder();
		prompt.append("지금부터 너가 생성해야 할 게시물의 내용을 알려줄게.\n");

		// 게시물 주제 설정
		prompt.append(topic).append("라는 주제에 대해서 게시물을 생성해야해.\n");

		// 게시물 목적 설정
		prompt.append("게시물의 목적은 ").append(purpose.getValue()).append("이야. ");
		switch (purpose) {
			case INFORMATION -> prompt.append("계정의 설정에 맞게 정보를 제공하는 글을 작성해.\n");
			case OPINION -> prompt.append("글의 주제에 대해, 계정 설정에 맞게 의견을 표출하는 글을 작성해.\n");
			case HUMOR -> prompt
				.append("글의 주제에 대해 유쾌한 글을 작성해. 이때 계정 설명에 유머에 대한 설명이 있다면 설명에 맞게 작성하고, 만약 설명이 없다면 너가 직접 웃긴 글을 작성해.\n");
			case MARKETING -> prompt.append("계정에 설정된 브랜드 정보를 바탕으로 홍보하는 글을 작성해.\n");
		}

		// 게시물 글자수 설정
		prompt.append("글자수는 ").append(length.getMaxLength()).append("자를 절대 초과해서는 안돼. 그 이내로 작성해.\n");

		// 게시물 핵심 내용 설정
		if (content != null) {
			prompt.append("다음과 같은 내용을 반드시 포함하도록 해: ").append(content);
		}

		return prompt.toString();
	}

	/**
	 * 게시물 그룹에 설정된 참고자료 Prompt.
	 * "뉴스를 참고하는" 게시물 그룹을 위해 뉴스에 대한 참고자료 Prompt를 생성
	 */
	public String getNewsRefPrompt(String summary, String content) {
		return "다음 뉴스 기사 내용을 바탕으로 게시물을 생성해줘:\n"
			+ "요약: " + summary + "\n"
			+ "본문: " + content;
	}

	/**
	 * 게시물 그룹에 설정된 참고자료 Prompt.
	 * "이미지를 참고하는" 게시물 그룹을 위해 이미지에 대한 참고자료 Prompt를 생성
	 */
	public String getImageRefPrompt() {
		return "다음 이미지 내용을 바탕으로 게시물을 생성해줘.";
	}
}
