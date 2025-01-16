package org.security.constants;

public final class HeaderConstants {
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String ACCESS_TOKEN_HEADER = "Authorization";
	public static final String REFRESH_TOKEN_HEADER = "RefreshToken";

	private HeaderConstants() {
		throw new UnsupportedOperationException("인스턴트를 생성할 수 없습니다.");
	}
}
