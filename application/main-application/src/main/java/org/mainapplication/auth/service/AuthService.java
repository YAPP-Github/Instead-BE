package org.mainapplication.auth.service;

import org.domainmodule.user.entity.User;
import org.mainapplication.global.oauth2.userinfo.OAuth2UserInfo;

import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
	void saveRenewRefreshToken(User user, String token);
	String reissueAccessToken(String refreshToken, HttpServletResponse response);
	void createAndSaveOauth(OAuth2UserInfo oAuth2Response, User user);
}

