package org.security.service;

import org.domainmodule.user.entity.RefreshToken;
import org.domainmodule.user.entity.User;
import org.domainmodule.user.repository.RefreshTokenRepository;
import org.domainmodule.user.service.AuthService;
import org.security.constants.HeaderConstants;
import org.security.jwt.JwtProvider;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtProvider jwtProvider;

	@Override
	public void saveRenewRefreshToken(User user, String token) {
		refreshTokenRepository.findByUserId(user.getId())
			.ifPresentOrElse(
				// 존재하면 갱신
				existingToken -> existingToken.renewToken(token),
				// 존재하지 않으면 생성
				() -> createAndSaveRefreshToken(user, token)
			);
	}

	@Override
	public void setTokensInResponse(HttpServletResponse response, String accessToken, String refreshToken) {
		response.setHeader(HeaderConstants.ACCESS_TOKEN_HEADER, HeaderConstants.TOKEN_PREFIX + accessToken);
		createHttpOnlyCookie(response, refreshToken);
	}

	@Override
	public String reissueAccessToken(String refreshToken, HttpServletResponse response) {
		if (jwtProvider.isTokenValid(refreshToken, false)) {
			throw new IllegalArgumentException("RefreshToken이 만료되었습니다.");
		}

		String usdrId = jwtProvider.extractUserId(refreshToken, false);
		validateRefreshToken(usdrId, refreshToken);

		String accessToken = jwtProvider.generateAccessToken(usdrId);
		response.setHeader(HeaderConstants.ACCESS_TOKEN_HEADER, HeaderConstants.TOKEN_PREFIX + accessToken);
		return jwtProvider.generateAccessToken(usdrId);
	}

	private void createHttpOnlyCookie(HttpServletResponse response, String refreshToken) {
		Cookie cookie = new Cookie(HeaderConstants.REFRESH_TOKEN_HEADER, refreshToken);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(7 * 24 * 60 * 60); //TODO 이후에 쿠키 유지시간 설정 유효기간 7일
		response.addCookie(cookie);
	}

	private void createAndSaveRefreshToken(User user, String token) {
		RefreshToken refreshToken = RefreshToken.builder().user(user).token(token).build();
		refreshTokenRepository.save(refreshToken);
	}

	private void validateRefreshToken(String userId, String refreshToken) {
		refreshTokenRepository.findByUserId(Long.valueOf(userId))
			.filter(storedToken -> storedToken.getToken().equals(refreshToken))
			.orElseThrow(() -> new IllegalArgumentException("RefreshToken이 일치하지 않습니다."));
	}
}
