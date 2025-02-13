package org.mainapp.domain.agent.controller.response;

import java.util.List;

import org.domainmodule.agent.entity.Agent;
import org.mainapp.domain.agent.controller.response.type.AgentResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "계정 목록 조회 API 응답 본문")
public record GetAgentsResponse(
	@Schema(description = "사용자 SNS 계정 리스트")
	List<AgentResponse> agents
) {

	public static GetAgentsResponse from(List<Agent> agents) {
		List<AgentResponse> agentResponses = agents.stream()
			.map(AgentResponse::from)
			.toList();
		return new GetAgentsResponse(agentResponses);
	}
}
