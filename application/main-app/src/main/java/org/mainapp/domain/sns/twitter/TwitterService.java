package org.mainapp.domain.sns.twitter;

import java.util.Map;

import org.domainmodule.agent.entity.Agent;
import org.domainmodule.sns.entity.SnsProvider;
import org.domainmodule.user.entity.User;
import org.mainapp.domain.agent.service.AgentService;
import org.mainapp.domain.sns.exception.SnsErrorCode;
import org.mainapp.domain.sns.token.SnsProviderService;
import org.mainapp.domain.sns.token.SnsTokenService;
import org.mainapp.domain.sns.twitter.request.OAuthClientCredentials;
import org.mainapp.domain.user.service.UserService;
import org.mainapp.global.constants.UrlConstants;
import org.mainapp.global.error.CustomException;
import org.mainapp.global.util.JwtUtil;
import org.snsclient.twitter.dto.response.TwitterToken;
import org.snsclient.twitter.dto.response.TwitterUserInfoDto;
import org.snsclient.twitter.facade.TwitterApiService;
import org.snsclient.util.TwitterOauthUtil;
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
	private final UserService userService;
	private final SnsProviderService snsProviderService;

	/**
	 * Twitter Authorization URL 생성 및 리다이렉트 ResponseEntity 반환
	 */
	public String createRedirectResponse(String accessToken, OAuthClientCredentials request) {
		final Long userId = jwtUtil.getUserIdFromAccessToken(accessToken);
		User user = userService.findUserById(userId);

		// ClientId, ClientSecret 저장
		SnsProvider snsProvider = snsProviderService.createSnsProvider(user, request.clientId(), request.clientSecret());

		// 리다이렉트 URL 생성
		return twitterApiService.getTwitterAuthorizationUrl(snsProvider.getId().toString(), snsProvider.getClientId());
	}

	/**
	 * Authorization Code를 처리하여 토큰 발급
	 * 사용자 계정(Agent)과 SNS 토큰(SnsToken)을 저장
	 * 로그인 이후 redirectUrl 리턴
	 */
	@Transactional
	public String loginOrRegister(String code, String encodedState) {
		Map<String, String> stateMap = TwitterOauthUtil.decodeStateFromBase64(encodedState);
		String snsProviderId = stateMap.get("providerId");

		// client key값 가져오기
		SnsProvider snsProvider =  snsProviderService.findSnsProviderById(Long.parseLong(snsProviderId));

		// 토큰 발급
		TwitterToken tokenResponse = twitterApiService.getTwitterAuthorizationToken(code, snsProvider.getClientId(), snsProvider.getClientSecret());

		// 유저 정보
		TwitterUserInfoDto userInfo = getTwitterUserInfo(tokenResponse);

		String userId = snsProvider.getUser().getId().toString();
		Agent agent = agentService.updateOrCreateAgent(userInfo, userId);
		snsTokenService.createOrUpdateSnsToken(agent, tokenResponse);

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
