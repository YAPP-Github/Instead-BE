package org.mainapp.domain.v1.agent.controller.response;

import org.domainmodule.agent.entity.Agent;
import org.domainmodule.agent.entity.type.AgentPlanType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "계정 요금제 조회 API 응답 본문")
public record GetAgentPlanResponse(
	@Schema(description = "계정 요금제 타입")
	AgentPlanType agentPlanType
) {
	public static GetAgentPlanResponse from(Agent agent) {
		return new GetAgentPlanResponse(agent.getAgentPlan());
	}
}
