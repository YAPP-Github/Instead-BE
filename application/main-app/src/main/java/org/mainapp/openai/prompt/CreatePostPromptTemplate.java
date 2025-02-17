package org.mainapp.openai.prompt;

import org.domainmodule.agent.entity.type.AgentToneType;
import org.domainmodule.postgroup.entity.type.PostGroupLengthType;
import org.domainmodule.postgroup.entity.type.PostGroupPurposeType;
import org.springframework.stereotype.Component;

@Component
public class CreatePostPromptTemplate {

	/**
	 * 계정 기본 정보 Instruction 임시 데이터
	 */
	// TODO: 지금은 시연을 위한 임시 데이터를 반환하게 해두었고, 실제 agent 생성하는 기능 추가되면 실제 agent 데이터 기반으로 프롬프트 구성해야 함
	public String getTempInstructionPrompt() {
		return
			"너는 한국의 20대 중반 여성이고, 너의 컨셉에 맞게 SNS에 올릴 게시물을 작성해야 해. 반드시 반말을 사용해서 작성하도록 하고, 너무 딱딱한 말투보다는 부드러운 어투를 사용해. 중간중간 이모지를 사용해도 좋아."
				+ "답변은 JSON 포맷으로 생성해줘. content에는 게시물의 본문이 들어가고, summary에는 본문의 핵심 내용을 명사형으로 요약한 제목이 들어가야 해.";
	}

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
		if (introduction != null) {
			prompt.append("계정의 전체적인 소개를 해줄게. ").append(introduction).append("\n");
		}
		if (domain != null) {
			prompt.append("계정의 다룰 분야는 다음과 같아. ").append(domain).append("\n");
		}
		if (tone != null && tone != AgentToneType.CUSTOM) {
			prompt
				.append("게시물을 생성할 때 말투는 ")
				.append(tone.getSuffix()).append("와 같이 ")
				.append(tone.getValue()).append("을 사용해야 해.\n");
		} else if (customTone != null && tone == AgentToneType.CUSTOM) {
			prompt
				.append("게시물을 생성할 때 말투는 다음과 같이 사용해야 해. ")
				.append(customTone);
		}

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
		return "지금부터 너가 생성해야 할 게시물의 내용을 알려줄게.\n"
			+ topic
			+ "라는 주제에 대해서 "
			+ purpose.getValue()
			+ " 목적의 글을 생성해줘.\n"
			+ "글자수는 "
			+ length.getMaxLength()
			+ "자를 절대 초과해서는 안돼. 그 이내로 작성해줘.\n"
			+ "다음과 같은 내용을 반드시 포함해줘: \n"
			+ content;
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
