package org.mainapp.domain.token.service;

import java.time.Duration;

import org.domainmodule.user.entity.RefreshToken;
import org.domainmodule.user.entity.User;
import org.domainmodule.user.repository.RefreshTokenRepository;
import org.mainapp.domain.token.exception.TokenErrorCode;
import org.mainapp.domain.user.service.UserServiceImpl;
import org.mainapp.global.constants.JwtProperties;
import org.mainapp.global.error.CustomException;
import org.mainapp.global.util.JwtUtil;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtUtil jwtUtil;
	private final JwtProperties jwtProperties;
	private final UserServiceImpl userService;

	/**
	 *  refreshToken 발급 후 저장
	 */
	@Transactional
	public String generateRefreshToken(Long userId) {
		String refreshToken = jwtUtil.generateRefreshToken(userId.toString());
		updateOrCreateRefreshToken(userId, refreshToken);
		return refreshToken;
	}

	/**
	 * RefreshToken이 존재하면 Token 업데이트, 존재하지 않으면 RefreshToken 새로 저장
	 */
	@Override
	@Transactional
	public void updateOrCreateRefreshToken(Long userId, String refreshToken) {
		refreshTokenRepository.findByUserId(userId)
			.ifPresentOrElse(
				// 존재하면 갱신
				existingToken -> updateRefreshToken(existingToken, refreshToken),
				// 존재하지 않으면 생성
				() -> createAndSaveRefreshToken(userId, refreshToken)
			);
	}

	private void updateRefreshToken(RefreshToken existingToken, String token) {
		existingToken.renewToken(token);
		refreshTokenRepository.save(existingToken);
	}

	private void createAndSaveRefreshToken(Long userId, String token) {
		User user = userService.findUserById(userId);
		RefreshToken refreshToken = RefreshToken.create(user, token, Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMS()).toSeconds());
		refreshTokenRepository.save(refreshToken);
	}

	public void checkRefreshTokenMatch(String userId, String refreshToken) {
		refreshTokenRepository.findByUserId(Long.valueOf(userId))
			.filter(storedToken -> storedToken.getToken().equals(refreshToken))
			.orElseThrow(() -> new CustomException(TokenErrorCode.REFRESH_TOKEN_NOT_MATCHED));
	}

	@Transactional
	public String getRefreshToken(Long userId) {
		return refreshTokenRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(TokenErrorCode.REFRESH_TOKEN_NOT_FOUND))
			.getToken();
	}
}
