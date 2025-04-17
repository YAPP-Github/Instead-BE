package org.mainapp.domain.v1.auth.service;

import org.domainmodule.user.entity.User;
import org.mainapp.global.oauth2.userinfo.OAuth2UserInfo;

public interface AuthService {
	void createAndSaveOauth(OAuth2UserInfo oAuth2Response, User user);

	User loginOrRegisterUser(OAuth2UserInfo oAuth2Response);

	void logout(String accessToken);
}

