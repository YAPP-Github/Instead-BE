package org.mainapp.domain.agent.service;

import org.domainmodule.agent.entity.Agent;
import org.domainmodule.agent.entity.type.AgentPlatformType;
import org.domainmodule.agent.repository.AgentRepository;
import org.domainmodule.user.entity.User;
import org.mainapp.domain.user.service.UserService;
import org.snsclient.twitter.dto.response.TwitterUserInfoDto;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgentService {
	private final AgentRepository agentRepository;
	private final UserService userService;

	/**
	 * X API 로그인을 성공한 후 Agent와 SnsToken 생성 및 저장
	 * @return 생성된 Agent 엔티티
	 */
	public Agent updateOrCreateAgent(TwitterUserInfoDto userInfo) {
		//TODO 임시 설정한 부분 (이후 securityContext에서 userId가져오기)
		long userId = 1L;
		User user = userService.findUserById(userId);

		// Agent가 이미 존재하는지 확인
		return agentRepository.findByAccountIdAndPlatform(userInfo.id(), AgentPlatformType.X)
			.map(existingAgent -> updatAgent(existingAgent, userInfo))
			.orElseGet(() -> createAndSaveAgent(user, userInfo));
	}

	private Agent createAndSaveAgent(User user, TwitterUserInfoDto userInfo) {
		Agent newAgent = Agent.create(
			user,
			AgentPlatformType.X,
			userInfo.id(),
			userInfo.description(),
			userInfo.profileImageUrl());
		return agentRepository.save(newAgent);
	}

	private Agent updatAgent(Agent agent, TwitterUserInfoDto userInfo) {
		agent.updateInfo(userInfo.description(), userInfo.profileImageUrl());
		return agentRepository.save(agent);
	}
}
