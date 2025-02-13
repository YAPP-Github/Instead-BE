package org.mainapp.domain.agent.controller;

import org.mainapp.domain.agent.controller.response.GetAgentsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/agents")
@RequiredArgsConstructor
public class AgentController {

	@GetMapping
	public ResponseEntity<GetAgentsResponse> getAgents() {
		return ResponseEntity.noContent().build();
	}
}
