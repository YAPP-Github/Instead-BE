package org.mainapp.domain.token.service;

import org.domainmodule.user.entity.User;

import jakarta.servlet.http.HttpServletResponse;

public interface TokenService {
	void saveRenewRefreshToken(User user, String newToken);
	String reissueAccessToken(String refreshToken, HttpServletResponse response);
}
