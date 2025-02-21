package org.mainapp.domain.agent.controller.request;

import org.domainmodule.agent.entity.AgentPersonalSetting;
import org.domainmodule.agent.entity.type.AgentToneType;
import org.springframework.lang.Nullable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "계정 개인화 설정 수정 API 요청 본문")
public record UpdateAgentPersonalSettingRequest(
	@Schema(description = "계정 분야", example = "IT 기술")
	@Size(max = 20, message = "계정 분야는 20자 이내로 작성해주세요.")
	@NotNull(message = "계정 분야를 입력해주세요.")
	String domain,
	@Schema(description = "계정 소개", example = "최신 IT 기술 소식을 전달하는 계정")
	@Size(max = 500, message = "계정 소개는 500자 이내로 작성해주세요.")
	@NotNull(message = "계정 소개를 입력해주세요.")
	String introduction,
	@Schema(description = "계정 말투에 대한 Enum", example = "CUSTOM")
	@Nullable
	AgentToneType tone,
	@Schema(description = "계정 말투가 CUSTOM인 경우 적용되는 사용자 설정 말투", example = "20대 중반의 차분한 여성같은 말투, 중간중간 이모지 사용")
	@Size(max = 500, message = "사용자 지정 말투는 500자 이내로 작성해주세요.")
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
