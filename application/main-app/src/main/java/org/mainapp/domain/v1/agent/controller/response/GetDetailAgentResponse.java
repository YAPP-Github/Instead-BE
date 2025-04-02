package org.mainapp.domain.v1.agent.controller.response;

import org.domainmodule.agent.entity.Agent;
import org.domainmodule.agent.entity.AgentPersonalSetting;
import org.mainapp.domain.v1.agent.controller.response.type.AgentPersonalSettingResponse;
import org.mainapp.domain.v1.agent.controller.response.type.AgentResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "계정 상세 조회 API 응답 본문")
public record GetDetailAgentResponse(
	@Schema(description = "사용자 SNS 계정 정보")
	AgentResponse agent,
	@Schema(description = "계정 개인화 설정 정보")
	AgentPersonalSettingResponse agentPersonalSetting
) {

	public static GetDetailAgentResponse from(
		Agent agent,
		AgentPersonalSetting personalSetting
	) {
		AgentResponse agentResponse = AgentResponse.from(agent);
		AgentPersonalSettingResponse agentPersonalSettingResponse = AgentPersonalSettingResponse.from(personalSetting);
		return new GetDetailAgentResponse(
			agentResponse,
			agentPersonalSettingResponse
		);
	}
}
