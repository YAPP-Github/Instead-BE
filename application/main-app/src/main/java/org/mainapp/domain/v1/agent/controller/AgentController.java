package org.mainapp.domain.v1.agent.controller;

import org.mainapp.domain.v1.agent.controller.request.UpdateAgentPersonalSettingRequest;
import org.mainapp.domain.v1.agent.controller.response.GetAgentPlanResponse;
import org.mainapp.domain.v1.agent.controller.response.GetAgentsResponse;
import org.mainapp.domain.v1.agent.controller.response.GetDetailAgentResponse;
import org.mainapp.domain.v1.agent.service.AgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/agents")
@RequiredArgsConstructor
@Tag(name = "Agent API", description = "사용자가 연동한 SNS 계정(에이전트)에 대한 요청을 처리하는 API입니다.")
public class AgentController {

	private final AgentService agentService;

	@Operation(summary = "계정 목록 조회 API", description = "사용자가 연동한 SNS 계정 목록을 조회합니다.")
	@GetMapping
	public ResponseEntity<GetAgentsResponse> getAgents() {
		return ResponseEntity.ok(agentService.getAgents());
	}

	@Operation(summary = "계정 상세 조회 API", description = "사용자가 연동한 SNS 계정의 상세 정보를 조회합니다.")
	@GetMapping("/{agentId}")
	public ResponseEntity<GetDetailAgentResponse> getDetailAgent(@PathVariable Long agentId) {
		return ResponseEntity.ok(agentService.getDetailAgent(agentId));
	}

	@Operation(
		summary = "계정 개인화 설정 수정 API",
		description = """
			사용자가 연동한 SNS 계정의 개인화 설정을 변경합니다.

			**변경되는 필드만 채워주시면 됩니다!**

			빈 문자열로는 변경할 수 없습니다."""
	)
	@PutMapping("/{agentId}/personal-setting")
	public ResponseEntity<Void> updateAgentPersonalSetting(
		@PathVariable Long agentId,
		@Validated @RequestBody UpdateAgentPersonalSettingRequest request
	) {
		agentService.updateAgentPersonalSetting(agentId, request);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "계정 요금제 플랜 조회", description = "사용자가 연동한 SNS 계정의 요금제를 조회합니다.")
	@GetMapping("/{agentId}/agent-plan")
	public ResponseEntity<GetAgentPlanResponse> getAgentPlan(@PathVariable Long agentId) {
		return ResponseEntity.ok(agentService.getAgentPlan(agentId));
	}
}
