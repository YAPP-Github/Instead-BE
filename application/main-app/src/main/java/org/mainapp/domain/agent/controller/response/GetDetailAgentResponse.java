package org.mainapp.domain.agent.controller.response;

import org.domainmodule.agent.entity.Agent;
import org.domainmodule.agent.entity.AgentPersonalSetting;
import org.mainapp.domain.agent.controller.response.type.AgentPersonalSettingResponse;
import org.mainapp.domain.agent.controller.response.type.AgentResponse;

public record GetDetailAgentResponse(
	AgentResponse agent,
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
