package org.mainapp.domain.token.service;

import org.domainmodule.user.entity.RefreshToken;
import org.domainmodule.user.entity.User;
import org.domainmodule.user.repository.RefreshTokenRepository;
import org.mainapp.domain.token.exception.TokenErrorCode;
import org.mainapp.global.error.CustomException;
import org.mainapp.global.util.JwtUtil;
import org.mainapp.global.util.ResponseUtil;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtUtil jwtUtil;

	@Override
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

	@Override
	@Transactional
	public String reissueAccessToken(String refreshToken, HttpServletResponse response) {
		if (jwtUtil.isTokenValid(refreshToken, false)) {
			throw new CustomException(TokenErrorCode.REFRESH_TOKEN_EXPIRED);
		}

		String usdrId = jwtUtil.extractUserId(refreshToken, false);
		validateRefreshToken(usdrId, refreshToken);

		String accessToken = jwtUtil.generateAccessToken(usdrId);
		ResponseUtil.setTokensInResponse(response, accessToken, refreshToken);
		return accessToken;
	}

	private void validateRefreshToken(String userId, String refreshToken) {
		refreshTokenRepository.findByUserId(Long.valueOf(userId))
			.filter(storedToken -> storedToken.getToken().equals(refreshToken))
			.orElseThrow(() -> new CustomException(TokenErrorCode.REFRESH_TOKEN_NOT_MATCHED));
	}
}
