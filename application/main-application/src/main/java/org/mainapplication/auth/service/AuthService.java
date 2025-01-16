package org.mainapplication.auth.service;

import org.domainmodule.user.entity.User;
import org.mainapplication.global.oauth2.userinfo.OAuth2UserInfo;

public interface AuthService {
	void createAndSaveOauth(OAuth2UserInfo oAuth2Response, User user);
}

