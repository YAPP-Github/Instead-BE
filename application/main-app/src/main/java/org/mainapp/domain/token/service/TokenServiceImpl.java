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
	private final ResponseUtil responseUtil;

	/**
	 *  refreshToken 발급 후 저장
	 */
	@Transactional
	public void generateRefreshToken(User user) {
		String refreshToken = jwtUtil.generateRegreshToken(user.getId().toString());
		createAndSaveRefreshToken(user, refreshToken);
	}

	/**
	 * User가 존재하면 Token 업데이트, 존재하지 않으면 RefreshToken 새로 저장
	 */
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

	/**
	 *  AccessToken 재발급
	 */
	@Override
	@Transactional
	public String reissueAccessToken(String refreshToken, HttpServletResponse response) {
		if (jwtUtil.isTokenValid(refreshToken, false)) {
			throw new CustomException(TokenErrorCode.REFRESH_TOKEN_EXPIRED);
		}

		String userId = jwtUtil.extractUserId(refreshToken, false);
		validateRefreshToken(userId, refreshToken);

		String accessToken = jwtUtil.generateAccessToken(userId);
		responseUtil.setTokensInResponse(response, accessToken, refreshToken);
		return accessToken;
	}

	private void validateRefreshToken(String userId, String refreshToken) {
		refreshTokenRepository.findByUserId(Long.valueOf(userId))
			.filter(storedToken -> storedToken.getToken().equals(refreshToken))
			.orElseThrow(() -> new CustomException(TokenErrorCode.REFRESH_TOKEN_NOT_MATCHED));
	}

	@Transactional
	public String getRefreshToken(String userId) {
		return refreshTokenRepository.findByUserId(Long.parseLong(userId))
			.orElseThrow(() -> new CustomException(TokenErrorCode.REFRESH_TOKEN_NOT_FOUND))
			.getToken();
	}
}
