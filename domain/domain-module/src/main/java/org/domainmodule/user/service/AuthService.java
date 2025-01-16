package org.domainmodule.user.service;

import org.domainmodule.user.entity.User;

import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
	void saveRenewRefreshToken(User user, String token);
	void setTokensInResponse(HttpServletResponse response, String accessToken, String refreshToken);
	String reissueAccessToken(String refreshToken, HttpServletResponse response);
}

