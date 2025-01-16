package org.mainapplication.global.oauth2.service;

import java.util.Map;

import org.domainmodule.user.entity.User;
import org.mainapplication.global.oauth2.CustomUserDetails;
import org.mainapplication.global.oauth2.userinfo.GoogleOAuth2UserInfo;
import org.mainapplication.global.oauth2.userinfo.OAuth2UserInfo;
import org.mainapplication.user.service.UserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

	private final UserService userService;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);

		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		OAuth2UserInfo oAuth2Response = getOAuth2UserInfo(oAuth2User, registrationId);
		User user = userService.loginOrCreateUser(oAuth2Response);
		return new CustomUserDetails(user);
	}

	private OAuth2UserInfo getOAuth2UserInfo(OAuth2User oAuth2User, String registrationId) {
		Map<String, Object> attributes = oAuth2User.getAttributes();
		switch (registrationId) {
			case "google":
				return GoogleOAuth2UserInfo.fromAttributes(attributes);
			default:
				throw new OAuth2AuthenticationException("지원되지 않는 로그인 제공자: " + registrationId);
		}
	}
}