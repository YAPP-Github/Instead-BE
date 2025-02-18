package org.mainapp.domain.agent.controller.response.type;

import java.time.LocalDateTime;

import org.domainmodule.agent.entity.Agent;
import org.domainmodule.agent.entity.type.AgentPlanType;
import org.domainmodule.agent.entity.type.AgentPlatformType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "SNS 계정 응답 객체")
public record AgentResponse(
	@Schema(description = "계정 id (instead 내)", example = "1")
	Long id,
	@Schema(description = "계정 최초 연동 일시", example = "2025-01-01T00:00:00.000Z")
	LocalDateTime createdAt,
	@Schema(description = "SNS 종류", example = "X")
	AgentPlatformType platform,
	@Schema(description = "SNS 계정 이름 (사용자가 입력한 ID)", example = "test123")
	String accountName,
	@Schema(description = "SNS 고유 id (Sns 계정 식별을 위한 값)", example = "1342312")
	String accountId,
	@Schema(description = "계정 한줄소개", example = "최신 AI 소식을 전해드려요!")
	String bio,
	@Schema(description = "계정 프로필 이미지 URL", example = "https://~")
	String profileImageUrl,
	@Schema(description = "계정 요금제 (외부 SNS 내)", example = "FREE")
	AgentPlanType agentPlan,
	@Schema(description = "Auto 모드 여부", example = "false")
	Boolean autoMode
) {

	public static AgentResponse from(Agent agent) {
		return new AgentResponse(
			agent.getId(),
			agent.getCreatedAt(),
			agent.getPlatform(),
			agent.getAccountName(),
			agent.getAccountId(),
			agent.getBio(),
			agent.getProfileImage(),
			agent.getAgentPlan(),
			agent.getAutoMode()
		);
	}
}
