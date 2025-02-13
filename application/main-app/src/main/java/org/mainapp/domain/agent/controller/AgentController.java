package org.mainapp.domain.agent.controller;

import org.mainapp.domain.agent.controller.response.GetAgentsResponse;
import org.mainapp.domain.agent.service.AgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/agents")
@RequiredArgsConstructor
public class AgentController {

	private final AgentService agentService;

	@GetMapping
	public ResponseEntity<GetAgentsResponse> getAgents() {
		return ResponseEntity.ok(agentService.getAgents());
	}
}
