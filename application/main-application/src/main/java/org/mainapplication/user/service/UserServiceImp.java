package org.mainapplication.user.service;

import org.domainmodule.user.entity.User;
import org.domainmodule.user.repository.UserRepository;
import org.mainapplication.auth.service.AuthService;
import org.mainapplication.global.oauth2.userinfo.OAuth2UserInfo;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

	private final UserRepository userRepository;
	private final AuthService authService;

	@Override
	public User loginOrCreateUser(OAuth2UserInfo oAuth2Response) {
		return userRepository.findByEmail(oAuth2Response.getEmail())
			.orElseGet(() -> registerUser(oAuth2Response));
	}

	@Override
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
