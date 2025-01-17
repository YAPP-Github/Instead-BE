package org.mainapplication.global.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.mainapplication.global.constants.HeaderConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
	@Value("${jwt.access-token-expiration}")
	public long ACCESS_TOKEN_VALID_MILL_TIME;

	@Value("${jwt.refresh-token-expiration}")
	public long REFRESH_TOKEN_VALID_MILL_TIME;

	@Value("${jwt.access-token-key}")
	private String ACCESS_SECRET_KEY;

	@Value("${jwt.refresh-token-key}")
	private String REFRESH_SECRET_KEY;

	private final String ISSUER = "YAPP_PROJECT";

	//accessToekn 발급
	public String generateAccessToken(String userId) {
		final Date now = new Date();

		return
			Jwts.builder()
				.setHeaderParam("typ", "JWT")
				.setSubject(userId)
				.setIssuer(ISSUER)
				.setIssuedAt(now)
				.setExpiration(new Date(now.getTime() + ACCESS_TOKEN_VALID_MILL_TIME))
				.signWith(getAccessTokenKey())
				.compact();
	}

	//refreshToken 발급
	public String generateRegreshToken(String userId) {
		final Date now = new Date();

		return Jwts.builder()
				.setHeaderParam("typ", "JWT")
				.setSubject(userId)
				.setIssuer(ISSUER)
				.setIssuedAt(now)
				.setExpiration(new Date(now.getTime() + REFRESH_TOKEN_VALID_MILL_TIME))
				.signWith(getRefreshTokenKey())
				.compact();

	}

	private Key getAccessTokenKey() {
		byte[] keyBytes = ACCESS_SECRET_KEY.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	private Key getRefreshTokenKey() {
		byte[] keyBytes = REFRESH_SECRET_KEY.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public boolean isTokenValid(String token, boolean isAccessToken) {
		try{
			return (!isTokenExpired(token, isAccessToken));
		} catch (SecurityException | MalformedJwtException e) {
			log.error("Invalid JWT Token", e);
		} catch (ExpiredJwtException e) {
			log.error("Expired JWT Token", e);
		} catch (IllegalArgumentException e) {
			log.error("JWT claims string is empty", e);
		} catch (UnsupportedJwtException e) {
			log.error("Unsupported JWT Token", e);
		} catch (JwtException e) {
			log.error("JWT Token is not valid", e);
		}

		return false;
	}

	private boolean isTokenExpired(String token, boolean isAccessToken) {
		return extractExpiration(token, isAccessToken).before(new Date());
	}

	private Date extractExpiration(String token, boolean isAccessToken) {
		return extractClaim(token, isAccessToken, Claims::getExpiration);
	}

	public String extractUserId(String token, boolean isAccessToken) {
		return extractClaim(token, isAccessToken, (claims) -> claims.get("sub", String.class));
	}

	private <T> T extractClaim(String token, boolean isAccessToken, Function<Claims, T> claimResolver) {
		Claims claims =  extractAllAccessTokenClaims(token, isAccessToken);
		return claimResolver.apply(claims);
	}

	private Claims extractAllAccessTokenClaims(String token, boolean isAccessToken) {
		Key signingKey = isAccessToken ? getAccessTokenKey() : getRefreshTokenKey();

		try {
			return Jwts.parserBuilder()
				.setSigningKey(signingKey)
				.build()
				.parseClaimsJws(token)
				.getBody();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		}
	}

	// Http 요청 헤더에서 토큰 추출
	public String resolveToken(@Nullable HttpServletRequest request, String header) {
		String authHeader = request.getHeader(header);
		if (authHeader == null) {
			return null;
		}
		return authHeader.replace(HeaderConstants.TOKEN_PREFIX, "");
	}

	public Authentication getAuthentication(String memberId) {
		// authorities는 지금 ROLE이 필요 없어서 null, credentials은 비밀번호가 들어가는데 jwt이므로 패스
		return new UsernamePasswordAuthenticationToken(memberId, null, null);
	}
}
