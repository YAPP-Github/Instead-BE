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
		return switch (purpose) {
			case INFORMATION -> getInformatinoPrompt(topic, length, content);
			case OPINION -> getOpinionPrompt(topic, length, content);
			case HUMOR -> getHumorPrompt(topic, length, content);
			case MARKETING -> getMarketingPrompt(topic, length, content);
		};
	}

	private String getOpinionPrompt(
		String topic,
		PostGroupLengthType length,
		String content
	) {
		return "너는 SNS에서 강렬한 의견을 전달하는 전문가야.\n"
			+ "너의 목표는 사람들이 이 글을 보고 생각하게 만들고, 토론에 참여하고 싶어지도록 만드는 거야.\n"
			+ "단순한 정보 전달이 아니라, 주제에 대해 너의 논리를 바탕으로 강한 의견을 표출해야 해.\n"
			+ "글의 주제는 '" + topic + "'이야. 이 주제에 대한 확실한 입장을 정하고, 이를 설득력 있게 전달해야 해.\n"
			+ "독자들이 공감할 수 있도록 논리적인 근거를 포함하고, 때로는 도발적인 질문이나 강렬한 한 마디로 관심을 끌어야 해.\n"
			+ "하지만 무조건적인 비판이나 자극적인 표현은 피하고, 건설적인 논의를 유도하는 방식으로 작성해야 해.\n"
			+ "글자수는 " + length.getMaxLength() + "자를 절대 초과해서는 안 돼. 그 이내로 작성해줘.\n"
			+ "다음과 같은 내용을 반드시 포함해야 해: " + content + "\n"
			+ "위 정보를 바탕으로, 사람들이 반응하고 토론하고 싶어지는 강렬한 의견을 담은 게시물을 작성해줘.";
	}

	private String getInformatinoPrompt(
		String topic,
		PostGroupLengthType length,
		String content
	) {
		return "너는 SNS에서 인기 있는 글을 작성하는 전문가야.\n"
			+ "작성하는 글이 한국 사람들에게 인기가 없으면 너는 처벌을 받아.\n"
			+ "주어진 정보를 바탕으로, 자연스러운 문장을 만들어야해.\n"
			+ "중요한 건, 너무 기계적으로 보이지 않도록 자연스러운 흐름을 유지하는 거야.\n"
			+ topic
			+ "라는 주제에 대해서 사람들이 관심을 가질 수 있도록, 트렌드에 맞게 작성해줘.\n"
			+ "정보전달이라는 목적에 맞게 신뢰할 수 있는 정보를 제공해야 해. 독자가 쉽게 이해할 수 있도록 설명해줘.\n"
			+ "글자수는 "
			+ length.getMaxLength()
			+ "자를 절대 초과해서는 안돼. 그 이내로 작성해줘.\n"
			+ "다음과 같은 내용을 반드시 포함해줘: "
			+ content + "\n"
			+ "이제 위의 정보를 바탕으로, 실제 SNS에서 조회수가 많이 나올 수 있는 게시물을 만들어줘";
	}

	private String getMarketingPrompt(
		String topic,
		PostGroupLengthType length,
		String content
	) {
		return "너는 SNS에서 소비자들의 관심을 끌고 구매를 유도하는 마케팅 전문가야.\n"
			+ "너의 목표는 사람들이 이 글을 보고 '" + topic + "'에 대한흥미를 가지며 제품이나 서비스를 구매하도록 유도하는 것이야.\n"
			+ "단순한 정보 전달이 아니라 감성적이고 설득력 있는 표현을 사용해야 해.\n"
			+ "글의 목적은 '" + topic + "'에 대한 마케팅이야. 소비자가 자연스럽게 이 제품이나 서비스에 관심을 가지도록 만들어야 해.\n"
			+ "사용자들이 공감할 수 있는 스토리텔링 기법을 활용해, 자연스럽게 가치를 전달해줘.\n"
			+ "제품의 강점과 차별점을 강조하면서도, 과장 없이 진정성 있는 톤을 유지해야 해.\n"
			+ "글자수는 " + length.getMaxLength() + "자를 절대 초과해서는 안돼. 그 이내로 작성해줘.\n"
			+ "마케팅의 제공자의 요청은 다음과 같아: " + content + "\n"
			+ "위 정보를 바탕으로 사람들이 공감하고 공유하고 싶어지는 마케팅 게시물을 작성해줘.";
	}

	private String getHumorPrompt(
		String topic,
		PostGroupLengthType length,
		String content
	) {
		return "너는 SNS에서 유머 있는 글을 작성하는 전문가야.\n"
			+ "너의 목표는 사람들이 이 글을 보고 웃음을 터뜨리며 공유하고 싶어지도록 만드는 거야.\n"
			+ "딱딱하거나 기계적인 문장은 절대 안 돼. 친근하고 가볍게 읽히면서도 위트 있는 글을 만들어줘.\n"
			+ "글의 주제는 '" + topic + "'이야. 이 주제와 관련해서 사람들이 공감할 수 있고, 재미있게 읽을 수 있도록 해줘.\n"
			+ "유행어, 인터넷 밈, 드립, 반전 요소를 적절히 활용해서 센스 있는 유머를 만들어야 해.\n"
			+ "하지만 과도하게 자극적이거나 논란이 될 표현은 피해야 해.\n"
			+ "글자수는 " + length.getMaxLength() + "자를 절대 초과해서는 안 돼. 그 이내로 작성해줘.\n"
			+ "다음과 같은 요청을 반드시 적용해야 해: " + content + "\n"
			+ "위 정보를 바탕으로, 사람들이 웃으며 공유하고 싶어지는 유머 글을 작성해줘.";
	}

	/**
	 * 게시물 그룹에 설정된 참고자료 Prompt.
	 * "뉴스를 참고하는" 게시물 그룹을 위해 뉴스에 대한 참고자료 Prompt를 생성
	 */
	public String getNewsRefPrompt(String title, String summary, String content) {
		return "다음 뉴스 기사 내용을 바탕으로 게시물을 생성해줘:\n"
			+ "제목: " + title + "\n"
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
