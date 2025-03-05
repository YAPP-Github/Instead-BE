package org.mainapp.domain.sns.twitter;

import org.domainmodule.agent.entity.Agent;
import org.mainapp.domain.agent.service.AgentService;
import org.mainapp.domain.sns.exception.SnsErrorCode;
import org.mainapp.domain.sns.token.SnsTokenService;
import org.mainapp.global.constants.UrlConstants;
import org.mainapp.global.error.CustomException;
import org.mainapp.global.util.JwtUtil;
import org.snsclient.twitter.dto.response.TwitterToken;
import org.snsclient.twitter.dto.response.TwitterUserInfoDto;
import org.snsclient.twitter.service.TwitterApiService;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import twitter4j.TwitterException;

@Service
@RequiredArgsConstructor
public class TwitterService {
	private final TwitterApiService twitterApiService;
	private final AgentService agentService;
	private final SnsTokenService snsTokenService;
	private final JwtUtil jwtUtil;

	/**
	 * Twitter Authorization URL 생성 및 리다이렉트 ResponseEntity 반환
	 */
	public String createRedirectResponse(String accessToken) {
		// 리다이렉트 URL 생성
		final Long userId = jwtUtil.getUserIdFromAccessToken(accessToken);
		return twitterApiService.getTwitterAuthorizationUrl(userId.toString());
	}

	/**
	 * Authorization Code를 처리하여 토큰 발급
	 * 사용자 계정(Agent)과 SNS 토큰(SnsToken)을 저장
	 * 로그인 이후 redirectUrl 리턴
	 */
	@Transactional
	public String loginOrRegister(String code, String userId) {
		TwitterToken tokenResponse = twitterApiService.getTwitterAuthorizationToken(code);
		TwitterUserInfoDto userInfo = getTwitterUserInfo(tokenResponse);

		Agent agent = agentService.updateOrCreateAgent(userInfo, userId);
		snsTokenService.createOrUpdateSnsToken(agent, tokenResponse);

		// TODO 리다이렉트 URL 트위터가 들어가도록 변경
		return UrlConstants.PROD_DOMAIN_URL + "/" + agent.getId();
	}

	private TwitterUserInfoDto getTwitterUserInfo(TwitterToken token) {
		try {
			return twitterApiService.getUserInfo(token.accessToken());
		} catch (TwitterException e) {
			throw new CustomException(SnsErrorCode.TWITTER_USER_INFO_FETCH_FAILED);
		}
	}
}
