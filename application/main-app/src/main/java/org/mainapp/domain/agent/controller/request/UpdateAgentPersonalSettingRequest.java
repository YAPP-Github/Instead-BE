package org.mainapp.domain.agent.controller.request;

import org.domainmodule.agent.entity.AgentPersonalSetting;
import org.domainmodule.agent.entity.type.AgentToneType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "계정 개인화 설정 수정 API 요청 본문")
public record UpdateAgentPersonalSettingRequest(
	@Schema(description = "계정 분야", example = "IT 기술")
	String domain,
	@Schema(description = "계정 소개", example = "최신 IT 기술 소식을 전달하는 계정")
	String introduction,
	@Schema(description = "계정 말투에 대한 Enum", example = "CUSTOM")
	AgentToneType tone,
	@Schema(description = "계정 말투가 CUSTOM인 경우 적용되는 사용자 설정 말투", example = "20대 중반의 차분한 여성같은 말투, 중간중간 이모지 사용")
	String customTone
) {

	public static UpdateAgentPersonalSettingRequest from(AgentPersonalSetting agentPersonalSetting) {
		return new UpdateAgentPersonalSettingRequest(
			agentPersonalSetting.getDomain(),
			agentPersonalSetting.getIntroduction(),
			agentPersonalSetting.getTone(),
			agentPersonalSetting.getCustomTone()
		);
	}
}
