package org.mainapplication.global.constants;

public final class UrlConstants {
	private UrlConstants() {throw new UnsupportedOperationException("Url상수 인스턴트를 생성할 수 없습니다.");}
	// Local 환경 상수
	public static final String LOCAL_SERVER_URL = "http://localhost:8080";
	public static final String LOCAL_DOMAIN_URL = "http://localhost:3000";
	public static final String LOCAL_SECURE_DOMAIN_URL = "https://localhost:3000";

	// Prod 환경 상수
	public static final String PROD_SERVER_URL = "http://localhost:8080";
	public static final String PROD_DOMAIN_URL = "http://localhost:3000";
	public static final String PROD_SECURE_DOMAIN_URL = "https://localhost:3000";

	// 공통 상수
	public static final String BASE_URI = "/yapp";
}
