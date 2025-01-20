package org.mainapplication.domain.user.service;

import org.domainmodule.user.entity.User;
import org.domainmodule.user.repository.UserRepository;
import org.mainapplication.global.oauth2.userinfo.OAuth2UserInfo;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	@Override
	@Transactional
	public User createAndSaveUser(OAuth2UserInfo oAuth2Response) {
		User user = User.createUser(oAuth2Response.getEmail(),oAuth2Response.getName());
		return userRepository.save(user);
	}
}
