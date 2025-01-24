package org.mainapplication.domain.sns.token;

import org.domainmodule.agent.entity.Agent;
import org.domainmodule.snstoken.entity.SnsToken;
import org.domainmodule.snstoken.repository.SnsTokenRepository;
import org.snsclient.twitter.dto.response.TwitterTokenResponse;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SnsTokenService {
	private final SnsTokenRepository snsTokenRepository;

	/**
	 * 사용자 계정(Agent)와 토큰 응답을 기반으로 SNS 토큰(SnsToken)을 저장
	 * @param agent 사용자 계정(Agent)
	 * @param tokenResponse Twitter API로부터 받은 토큰 응답
	 */
	public void createOrUpdateSnsToken(Agent agent, TwitterTokenResponse tokenResponse) {
		snsTokenRepository.findByAgentId(agent.getId())
			.ifPresentOrElse(
				existingToken -> updateSnsToken(existingToken, tokenResponse),
				() -> saveNewSnsToken(agent, tokenResponse)
			);
	}

	private void updateSnsToken(SnsToken snsToken, TwitterTokenResponse tokenResponse) {
		snsToken.update(
			tokenResponse.accessToken(),
			tokenResponse.refreshToken(),
			tokenResponse.expiresIn()
		);
		snsTokenRepository.save(snsToken);
	}

	private void saveNewSnsToken(Agent agent, TwitterTokenResponse tokenResponse) {
		SnsToken newToken = SnsToken.create(
			agent,
			tokenResponse.accessToken(),
			tokenResponse.refreshToken(),
			tokenResponse.expiresIn()
		);
		snsTokenRepository.save(newToken);
	}

}
