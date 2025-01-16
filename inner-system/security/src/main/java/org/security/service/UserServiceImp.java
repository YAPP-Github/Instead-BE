package org.security.service;

import org.commonmodule.dto.OAuth2Response;
import org.domainmodule.user.entity.Oauth;
import org.domainmodule.user.entity.User;
import org.domainmodule.user.entity.type.ProviderType;
import org.domainmodule.user.repository.OauthRepository;
import org.domainmodule.user.repository.UserRepository;
import org.domainmodule.user.service.UserService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

	private final UserRepository userRepository;
	private final OauthRepository oauthRepository;

	@Override
	public User loginOrCreateUser(OAuth2Response oAuth2Response) {
		return userRepository.findByEmail(oAuth2Response.getEmail())
			.orElseGet(() -> registerUser(oAuth2Response));
	}

	@Override
	public User registerUser(OAuth2Response oAuth2Response) {
		User user = createAndSaveUser(oAuth2Response);
		createAndSaveOauth(oAuth2Response, user);
		return user;
	}

	private void createAndSaveOauth(OAuth2Response oAuth2Response, User user) {
		ProviderType providerType = ProviderType.fromValue(oAuth2Response.getProvider());
		Oauth oauth = Oauth.createOauth(user, providerType, oAuth2Response.getProviderId());
		oauthRepository.save(oauth);
	}


	private User createAndSaveUser(OAuth2Response oAuth2Response) {
		User user = User.createUser(oAuth2Response.getEmail(),oAuth2Response.getName());
		return userRepository.save(user);
	}
}
