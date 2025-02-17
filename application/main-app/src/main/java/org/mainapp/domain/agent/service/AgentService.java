package org.mainapp.domain.agent.service;

import java.util.List;

import org.domainmodule.agent.entity.Agent;
import org.domainmodule.agent.entity.AgentPersonalSetting;
import org.domainmodule.agent.entity.type.AgentPlatformType;
import org.domainmodule.agent.entity.type.AgentToneType;
import org.domainmodule.agent.repository.AgentPersonalSettingRepository;
import org.domainmodule.agent.repository.AgentRepository;
import org.domainmodule.user.entity.User;
import org.mainapp.domain.agent.controller.request.UpdateAgentPersonalSettingRequest;
import org.mainapp.domain.agent.controller.response.GetAgentPlanResponse;
import org.mainapp.domain.agent.controller.response.GetAgentsResponse;
import org.mainapp.domain.agent.controller.response.GetDetailAgentResponse;
import org.mainapp.domain.agent.exception.AgentErrorCode;
import org.mainapp.domain.user.service.UserService;
import org.mainapp.global.error.CustomException;
import org.mainapp.global.util.SecurityUtil;
import org.snsclient.twitter.dto.response.TwitterUserInfoDto;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgentService {

	private final AgentTransactionService agentTransactionService;
	private final UserService userService;
	private final AgentRepository agentRepository;
	private final AgentPersonalSettingRepository agentPersonalSettingRepository;

	/**
	 * X API 로그인을 성공한 후 Agent와 SnsToken 생성 및 저장
	 * @return 생성된 Agent 엔티티
	 */
	public Agent updateOrCreateAgent(TwitterUserInfoDto userInfo) {
		// 사용자 인증 정보 조회
		Long userId = SecurityUtil.getCurrentUserId();
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
			userInfo.profileImageUrl(),
			userInfo.subscriptionType()
		);
		AgentPersonalSetting newAgentPersonalSetting = AgentPersonalSetting.create(
			newAgent, "", "", AgentToneType.LESS_FORMAL, "");
		return agentTransactionService.saveAgentAndPersonalSetting(newAgent, newAgentPersonalSetting);
	}

	private Agent updatAgent(Agent agent, TwitterUserInfoDto userInfo) {
		agent.updateInfo(userInfo.description(), userInfo.profileImageUrl(), userInfo.subscriptionType());
		return agentTransactionService.saveAgent(agent);
	}

	/**
	 * 사용자에 해당하는 계정 목록을 조회하는 메서드
	 */
	public GetAgentsResponse getAgents() {
		// 사용자 인증 정보 조회
		Long userId = SecurityUtil.getCurrentUserId();

		// 사용자 계정 목록 조회
		List<Agent> agents = agentRepository.findAllByUserId(userId);

		// 반환
		return GetAgentsResponse.from(agents);
	}

	/**
	 * 개인화 설정 정보가 포함된 계정 상세 정보를 조회하는 메서드
	 */
	public GetDetailAgentResponse getDetailAgent(Long agentId) {
		// 사용자 인증 정보 조회
		Long userId = SecurityUtil.getCurrentUserId();

		// 사용자 계정 및 개인화 설정 조회
		AgentPersonalSetting agentPersonalSetting = agentPersonalSettingRepository.findByUserIdAndAgentId(
			userId, agentId).orElseThrow(() -> new CustomException(AgentErrorCode.AGENT_NOT_FOUND));

		// 반환
		return GetDetailAgentResponse.from(agentPersonalSetting.getAgent(), agentPersonalSetting);
	}

	/**
	 * 계정의 개인화 설정을 변경하는 메서드
	 */
	public void updateAgentPersonalSetting(Long agentId, UpdateAgentPersonalSettingRequest request) {
		// 사용자 인증 정보 조회
		Long userId = SecurityUtil.getCurrentUserId();

		// 사용자 계정 및 개인화 설정 조회
		AgentPersonalSetting agentPersonalSetting = agentPersonalSettingRepository.findByUserIdAndAgentId(
			userId, agentId).orElseThrow(() -> new CustomException(AgentErrorCode.AGENT_NOT_FOUND));

		// 개인화 설정 수정
		if (request.domain() != null) {
			agentPersonalSetting.updateDomain(request.domain());
		}
		if (request.introduction() != null) {
			agentPersonalSetting.updateIntroduction(request.introduction());
		}
		if (request.tone() != null) {
			agentPersonalSetting.updateTone(request.tone());
		}
		if (request.customTone() != null) {
			agentPersonalSetting.updateCustomTone(request.customTone());
		}

		// 변경사항 저장
		agentTransactionService.saveAgentPersonalSetting(agentPersonalSetting);
	}

	public GetAgentPlanResponse getAgentPlan(Long agentId) {
		Long userId = SecurityUtil.getCurrentUserId();
		Agent agent = agentRepository.findByUserIdAndId(userId, agentId)
			.orElseThrow(() -> new CustomException(AgentErrorCode.AGENT_NOT_FOUND));
		return GetAgentPlanResponse.from(agent);
	}
}
