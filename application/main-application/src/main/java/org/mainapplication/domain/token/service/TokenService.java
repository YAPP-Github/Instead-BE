package org.mainapplication.domain.token.service;

import org.domainmodule.user.entity.RefreshToken;
import org.domainmodule.user.entity.User;
import org.domainmodule.user.repository.RefreshTokenRepository;
import org.mainapplication.global.jwt.JwtProvider;
import org.mainapplication.global.util.ResponseUtil;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtProvider jwtProvider;

	@Transactional
	public void saveRenewRefreshToken(User user, String newToken) {
		refreshTokenRepository.findByUserId(user.getId())
			.ifPresentOrElse(
				// 존재하면 갱신
				existingToken -> updateRefreshToken(existingToken, newToken),
				// 존재하지 않으면 생성
				() -> createAndSaveRefreshToken(user, newToken)
			);
	}

	private void updateRefreshToken(RefreshToken existingToken, String token) {
		existingToken.renewToken(token);
		refreshTokenRepository.save(existingToken);
	}

	private void createAndSaveRefreshToken(User user, String token) {
		RefreshToken refreshToken = RefreshToken.builder().user(user).token(token).build();
		refreshTokenRepository.save(refreshToken);
	}

	@Transactional
	public String reissueAccessToken(String refreshToken, HttpServletResponse response) {
		if (jwtProvider.isTokenValid(refreshToken, false)) {
			throw new IllegalArgumentException("RefreshToken이 만료되었습니다.");
		}

		String usdrId = jwtProvider.extractUserId(refreshToken, false);
		validateRefreshToken(usdrId, refreshToken);

		String accessToken = jwtProvider.generateAccessToken(usdrId);
		ResponseUtil.setTokensInResponse(response, accessToken, refreshToken);
		return accessToken;
	}

	private void validateRefreshToken(String userId, String refreshToken) {
		refreshTokenRepository.findByUserId(Long.valueOf(userId))
			.filter(storedToken -> storedToken.getToken().equals(refreshToken))
			.orElseThrow(() -> new IllegalArgumentException("RefreshToken이 일치하지 않습니다."));
	}
}
