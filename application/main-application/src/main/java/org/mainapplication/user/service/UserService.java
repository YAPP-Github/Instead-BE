package org.mainapplication.user.service;

import org.domainmodule.user.entity.User;
import org.mainapplication.global.oauth2.userinfo.OAuth2UserInfo;

public interface UserService {
	User loginOrCreateUser(OAuth2UserInfo oAuth2Response);
	User registerUser(OAuth2UserInfo oAuth2Response);
}
