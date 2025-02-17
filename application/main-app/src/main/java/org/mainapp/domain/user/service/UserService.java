package org.mainapp.domain.user.service;

import org.domainmodule.user.entity.User;
import org.mainapp.domain.user.controller.response.UserInfoResponse;
import org.mainapp.global.oauth2.userinfo.OAuth2UserInfo;

public interface UserService {
	User createAndSaveUser(OAuth2UserInfo oAuth2Response);
	User findUserById(Long userId);
	UserInfoResponse getUserInfo();
}
