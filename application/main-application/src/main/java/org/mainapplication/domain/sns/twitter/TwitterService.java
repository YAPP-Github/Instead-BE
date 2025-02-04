package org.mainapplication.domain.sns.twitter;

import java.net.URI;

import org.domainmodule.agent.entity.Agent;
import org.mainapplication.domain.agent.service.AgentService;
import org.mainapplication.domain.sns.token.SnsTokenService;
import org.snsclient.twitter.dto.response.TwitterToken;
import org.snsclient.twitter.dto.response.TwitterUserInfoDto;
import org.snsclient.twitter.service.TwitterApiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TwitterService {
	private final TwitterApiService twitterApiService;
	private final AgentService agentService;
	private final SnsTokenService snsTokenService;

	/**
	 * Twitter Authorization URL 생성 및 리다이렉트 ResponseEntity 반환
	 */
	public ResponseEntity<Void> createRedirectResponse() {
		// 리다이렉트 URL 생성
		String redirectUrl = twitterApiService.getTwitterAuthorizationUrl();
		return ResponseEntity.status(HttpStatus.SEE_OTHER)
			.location(URI.create(redirectUrl))
			.build();
	}

	/**
	 * Authorization Code를 처리하여 토큰 발급
	 * 사용자 계정(Agent)과 SNS 토큰(SnsToken)을 저장
	 */
	@Transactional
	public void loginOrRegister(String code) {
		TwitterToken tokenResponse = twitterApiService.getTwitterAuthorizationToken(code);
		TwitterUserInfoDto userInfo = twitterApiService.getUserInfoWithRetry(tokenResponse.accessToken(), tokenResponse.refreshToken());

		Agent agent = agentService.findOrCreateAgent(userInfo);
		snsTokenService.createOrUpdateSnsToken(agent, tokenResponse);
	}
}
