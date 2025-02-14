package org.mainapp.domain.token.service;

public interface TokenService {
	void updateOrCreateRefreshToken(Long userId, String newToken);
	String getRefreshToken(Long userId);
}
