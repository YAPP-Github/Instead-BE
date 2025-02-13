package org.mainapp.domain.agent.controller;

import org.mainapp.domain.agent.controller.response.GetAgentsResponse;
import org.mainapp.domain.agent.controller.response.GetDetailAgentResponse;
import org.mainapp.domain.agent.service.AgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/agents")
@RequiredArgsConstructor
@Tag(name = "Agent API", description = "사용자가 연동한 SNS 계정(에이전트)에 대한 요청을 처리하는 API입니다.")
public class AgentController {

	private final AgentService agentService;

	@Operation(summary = "계정 목록 조회 API", description = "사용자가 연동한 SNS 계정 목록을 조회합니다.")
	@GetMapping
	public ResponseEntity<GetAgentsResponse> getAgents() {
		return ResponseEntity.ok(agentService.getAgents());
	}

	@GetMapping("/{agentId}")
	public ResponseEntity<GetDetailAgentResponse> getDetailAgent(@PathVariable Long agentId) {
		return ResponseEntity.ok(agentService.getDetailAgent(agentId));
	}
}
