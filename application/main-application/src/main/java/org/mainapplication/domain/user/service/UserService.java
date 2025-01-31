package org.mainapplication.domain.user.service;

import org.domainmodule.user.entity.User;
import org.mainapplication.global.oauth2.userinfo.OAuth2UserInfo;

public interface UserService {
	User createAndSaveUser(OAuth2UserInfo oAuth2Response);
	User findUserById(Long userId);
}
