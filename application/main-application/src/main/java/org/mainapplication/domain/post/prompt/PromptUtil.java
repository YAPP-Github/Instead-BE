package org.mainapplication.domain.post.prompt;

import org.mainapplication.domain.post.controller.request.CreatePostsRequest;
import org.springframework.stereotype.Component;

@Component
public class PromptUtil {

	/**
	 * 계정 기본 정보 Instruction.
	 * 에이전트의 정보를 받아서 기본 정보에 대한 Instruction을 생성
	 */
	// TODO: 지금은 시연을 위한 더미 데이터를 반환하게 해두었고, 실제 agent 생성하는 기능 추가되면 실제 agent 데이터 기반으로 프롬프트 구성해야 함
	public String getInstruction() {
		return "너는 한국의 20대 중반 남성이고, 너의 컨셉에 맞게 SNS에 올릴 게시물을 작성해야 해. 반드시 반말을 사용해서 작성하도록 해.";
	}

	/**
	 * 게시물 그룹에 설정된 주제 Prompt.
	 * 게시물 그룹의 정보를 받아서 게시물 주제에 대한 Prompt를 생성
	 */
	public String getBasicTopicPrompt(CreatePostsRequest request) {
		return "지금부터 너가 생성해야 할 게시물의 내용을 알려줄게.\n"
			+ request.getTopic()
			+ "라는 주제에 대해서 "
			+ request.getPurpose().getValue()
			+ " 목적의 글을 생성해줘.\n"
			+ "글자수는 "
			+ request.getLength().getMaxLength()
			+ "자 정도로 생성하고, 절대 초과해서는 안돼.\n"
			+ "다음과 같은 내용을 반드시 포함해줘: \n"
			+ request.getContent();
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
