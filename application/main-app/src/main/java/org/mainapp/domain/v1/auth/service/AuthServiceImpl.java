package org.mainapp.domain.v1.auth.service;

import java.util.Optional;

import org.domainmodule.user.entity.Oauth;
import org.domainmodule.user.entity.User;
import org.domainmodule.user.entity.type.ProviderType;
import org.domainmodule.user.repository.OauthRepository;
import org.mainapp.domain.v1.token.service.TokenServiceImpl;
import org.mainapp.domain.v1.user.service.UserServiceImpl;
import org.mainapp.global.oauth2.userinfo.OAuth2UserInfo;
import org.mainapp.global.util.JwtUtil;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final OauthRepository oauthRepository;
	private final UserServiceImpl userService;
	private final TokenServiceImpl tokenService;
	private final JwtUtil jwtUtil;

	/**
	 * Oauth2 Provider, ProviderId로 사용자 존재여부 확인후 없으면 회원가입 진행한다.
	 * @param oAuth2Response
	 * @return User
	 */
	@Override
	@Transactional
	public User loginOrRegisterUser(OAuth2UserInfo oAuth2Response) {
		return findUserByOAuthInfo(oAuth2Response)
			.orElseGet(() -> registerUser(oAuth2Response));
	}

	// 로그인
	private Optional<User> findUserByOAuthInfo(OAuth2UserInfo oAuth2Response) {
		ProviderType providerType = ProviderType.fromValue(oAuth2Response.getProvider());

		return oauthRepository.findByProviderAndProviderId(providerType, oAuth2Response.getProviderId())
			.map(Oauth::getUser)
			.map(user -> {
				tokenService.generateRefreshToken(user.getId());
				return user;
			});
	}

	// 회원가입
	private User registerUser(OAuth2UserInfo oAuth2Response) {
		// 유저 생성
		User user = userService.createAndSaveUser(oAuth2Response);
		// Oauth 테이블 생성
		createAndSaveOauth(oAuth2Response, user);
		// RefreshToken 테이블 생성
		tokenService.generateRefreshToken(user.getId());
		return user;
	}

	@Override
	@Transactional
	public void createAndSaveOauth(OAuth2UserInfo oAuth2Response, User user) {
		ProviderType providerType = ProviderType.fromValue(oAuth2Response.getProvider());
		Oauth oauth = Oauth.createOauth(user, providerType, oAuth2Response.getProviderId());
		oauthRepository.save(oauth);
	}

	/**
	 * 서비스 로그아웃 ( RefreshToken 제거 )
	 */
	@Override
	@Transactional
	public void logout(String accessToken) {
		final Long userId = jwtUtil.getUserIdFromAccessToken(accessToken);
		tokenService.deleteRefreshToken(userId);
	}
}
