package org.mainapp.domain.auth.service;

import java.util.Optional;

import org.domainmodule.user.entity.Oauth;
import org.domainmodule.user.entity.User;
import org.domainmodule.user.entity.type.ProviderType;
import org.domainmodule.user.repository.OauthRepository;
import org.mainapp.domain.user.service.UserServiceImpl;
import org.mainapp.global.oauth2.userinfo.OAuth2UserInfo;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final OauthRepository oauthRepository;
	private final UserServiceImpl userService;

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
			.map(Oauth::getUser);
	}

	// 회원가입
	private User registerUser(OAuth2UserInfo oAuth2Response) {
		User user = userService.createAndSaveUser(oAuth2Response);
		createAndSaveOauth(oAuth2Response, user);
		return user;
	}

	@Override
	@Transactional
	public void createAndSaveOauth(OAuth2UserInfo oAuth2Response, User user) {
		ProviderType providerType = ProviderType.fromValue(oAuth2Response.getProvider());
		Oauth oauth = Oauth.createOauth(user, providerType, oAuth2Response.getProviderId());
		oauthRepository.save(oauth);
	}
}
