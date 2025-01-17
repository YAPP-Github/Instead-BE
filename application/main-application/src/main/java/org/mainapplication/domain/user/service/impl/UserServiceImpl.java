package org.mainapplication.domain.user.service.impl;

import org.domainmodule.user.entity.Oauth;
import org.domainmodule.user.entity.User;
import org.domainmodule.user.entity.type.ProviderType;
import org.domainmodule.user.repository.OauthRepository;
import org.domainmodule.user.repository.UserRepository;
import org.mainapplication.domain.auth.service.AuthService;
import org.mainapplication.domain.user.service.UserService;
import org.mainapplication.global.oauth2.userinfo.OAuth2UserInfo;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final AuthService authService;
	private final OauthRepository oauthRepository;

	/**
	 * Oauth2 Provider, ProviderId로 사용자 존재여부 확인후 없으면 회원가입 진행한다.
	 * @param oAuth2Response
	 * @return User
	 */
	@Override
	@Transactional
	public User loginOrCreateUser(OAuth2UserInfo oAuth2Response) {
		ProviderType providerType = ProviderType.fromValue(oAuth2Response.getProvider());
		return oauthRepository.findByProviderAndProviderId(providerType, oAuth2Response.getProviderId())
			.map(Oauth::getUser)
			.orElseGet(() -> registerUser(oAuth2Response));
	}

	@Override
	@Transactional
	public User registerUser(OAuth2UserInfo oAuth2Response) {
		User user = createAndSaveUser(oAuth2Response);
		authService.createAndSaveOauth(oAuth2Response, user);
		return user;
	}

	private User createAndSaveUser(OAuth2UserInfo oAuth2Response) {
		User user = User.createUser(oAuth2Response.getEmail(),oAuth2Response.getName());
		return userRepository.save(user);
	}
}
