package org.mainapplication.global.constants;

public final class UrlConstants {
	private UrlConstants() {throw new UnsupportedOperationException("Url상수 인스턴트를 생성할 수 없습니다.");}
	// Local 환경 상수
	public static final String LOCAL_DOMAIN_URL = "http://localhost:3000";

	// Prod 환경 상수
	public static final String PROD_DOMAIN_URL = "https://instd.vercel.app";
	public static final String SERVER_DOMAIN_URL = "https://hong-nuri.shop";

	// 공통 상수
	public static final String BASE_URI = "/yapp";
}
