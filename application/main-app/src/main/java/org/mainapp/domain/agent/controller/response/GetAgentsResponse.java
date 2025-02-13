package org.mainapp.domain.agent.controller.response;

import java.util.List;

import org.domainmodule.agent.entity.Agent;
import org.mainapp.domain.agent.controller.response.type.AgentResponse;

public record GetAgentsResponse(
	List<AgentResponse> agents
) {

	public static GetAgentsResponse from(List<Agent> agents) {
		List<AgentResponse> agentResponses = agents.stream()
			.map(AgentResponse::from)
			.toList();
		return new GetAgentsResponse(agentResponses);
	}
}
