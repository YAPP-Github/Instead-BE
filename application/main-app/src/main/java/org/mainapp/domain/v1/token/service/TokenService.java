package org.mainapp.domain.v1.token.service;

public interface TokenService {
	void updateOrCreateRefreshToken(Long userId, String newToken);

	String getRefreshToken(Long userId);
}
