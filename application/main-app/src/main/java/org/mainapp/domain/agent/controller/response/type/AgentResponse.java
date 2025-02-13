package org.mainapp.domain.agent.controller.response.type;

import java.time.LocalDateTime;

import org.domainmodule.agent.entity.Agent;
import org.domainmodule.agent.entity.type.AgentPlanType;
import org.domainmodule.agent.entity.type.AgentPlatformType;

public record AgentResponse(
	LocalDateTime createdAt,
	Long agentId,
	AgentPlatformType platform,
	String accountId,
	String bio,
	String profileImageUrl,
	AgentPlanType agentPlan,
	Boolean autoMode
) {

	public static AgentResponse from(Agent agent) {
		return new AgentResponse(
			agent.getCreatedAt(),
			agent.getId(),
			agent.getPlatform(),
			agent.getAccountId(),
			agent.getBio(),
			agent.getProfileImage(),
			agent.getAgentPlan(),
			agent.getAutoMode()
		);
	}
}
