package org.mainapp.domain.v1.agent.service;

import org.domainmodule.agent.entity.Agent;
import org.domainmodule.agent.entity.AgentPersonalSetting;
import org.domainmodule.agent.repository.AgentPersonalSettingRepository;
import org.domainmodule.agent.repository.AgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgentTransactionService {

	private final AgentRepository agentRepository;
	private final AgentPersonalSettingRepository agentPersonalSettingRepository;

	@Transactional
	public Agent saveAgent(Agent agent) {
		return agentRepository.save(agent);
	}

	@Transactional
	public AgentPersonalSetting saveAgentPersonalSetting(AgentPersonalSetting agentPersonalSetting) {
		return agentPersonalSettingRepository.save(agentPersonalSetting);
	}

	@Transactional
	public Agent saveAgentAndPersonalSetting(
		Agent agent,
		AgentPersonalSetting agentPersonalSetting
	) {
		Agent savedAgent = agentRepository.save(agent);
		agentPersonalSettingRepository.save(agentPersonalSetting);
		return savedAgent;
	}
}
