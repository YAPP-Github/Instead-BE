package com.yapp.web1team.api.v1.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yapp.web1team.api.v1.auth.dto.request.AuthRequest;
import com.yapp.web1team.api.v1.auth.dto.response.SignInResponse;
import com.yapp.web1team.api.v1.auth.entity.RefreshToken;
import com.yapp.web1team.api.v1.auth.repository.RefreshTokenRepository;
import com.yapp.web1team.api.v1.user.entity.User;
import com.yapp.web1team.api.v1.user.service.UserService;
import com.yapp.web1team.security.constants.HeaderConstants;
import com.yapp.web1team.security.util.JwtProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;


@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final RefreshTokenRepository refreshTokenRepository;
	public void signUp(AuthRequest request) {

		if (userService.existsByAccount(request.account())) {
			throw new RuntimeException("중복된 회원입니다");
		}
		User user = User.create(request.account(), request.password());
		userService.saveUser(user);
	}

	public SignInResponse signIn(AuthRequest request, HttpServletResponse response) {
		User user = userService.findUserByAccount(request.account());

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
		}

		String accessToken = jwtProvider.generateAccessToken(user.getId().toString());
		String refreshToken = jwtProvider.generateRegreshToken(user.getId().toString());

		saveRenewRefreshToken(user, refreshToken);
		setTokensInResponse(response, accessToken, refreshToken);

		return SignInResponse.of(user.getAccount(), accessToken, refreshToken);
	}

	private void setTokensInResponse(HttpServletResponse response, String accessToken, String refreshToken) {
		response.setHeader(HeaderConstants.ACCESS_TOKEN_HEADER, HeaderConstants.TOKEN_PREFIX + accessToken);
		createHttpOnlyCookie(response, refreshToken);
	}

	private void createHttpOnlyCookie(HttpServletResponse response, String refreshToken) {
		Cookie cookie = new Cookie(HeaderConstants.REFRESH_TOKEN_HEADER, refreshToken);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(7 * 24 * 60 * 60); // 유효기간 7일
		response.addCookie(cookie);
	}

	private void saveRenewRefreshToken(User user, String token) {
		RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId()).orElse(null);

		if (refreshToken == null) {
			initRefreshToken(user, token); // 최초가입 후 로그인 시 Refresh Token이 존재하지 않음
		} else {
			refreshToken.renewToken(token);
		}
	}

	private void initRefreshToken(User user, String token) {
		RefreshToken refreshToken = RefreshToken.builder().user(user).token(token).build();
		refreshTokenRepository.save(refreshToken);
	}
	public String reissueAccessToken(String refreshToken, HttpServletResponse response) {
		if (jwtProvider.isTokenValid(refreshToken, false)) {
			throw new IllegalArgumentException("RefreshToken이 만료되었습니다.");
		}

		String userId = jwtProvider.extractUserId(refreshToken, false);
		validateRefreshToken(userId, refreshToken);

		String accessToken = jwtProvider.generateAccessToken(userId);
		response.setHeader(HeaderConstants.ACCESS_TOKEN_HEADER, HeaderConstants.TOKEN_PREFIX + accessToken);
		return jwtProvider.generateAccessToken(userId);
	}

	private void validateRefreshToken(String userId, String refreshToken) {
		refreshTokenRepository.findByUserId(Long.valueOf(userId))
			.filter(storedToken -> storedToken.getToken().equals(refreshToken))
			.orElseThrow(() -> new IllegalArgumentException("RefreshToken이 일치하지 않습니다."));
	}

}

