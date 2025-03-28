package org.snsclient.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TwitterOauthUtil {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	// JSON 데이터를 Base64 형식으로 인코딩
	public static String encodeStateToBase64(String providerId) {
		try {
			String jsonState = objectMapper.writeValueAsString(Map.of("providerId", providerId));
			return Base64.getUrlEncoder().encodeToString(jsonState.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new RuntimeException("Twitter Oauth encode 실패", e);
		}
	}

	// Base64 형식을 디코딩
	public static Map<String, String> decodeStateFromBase64(String base64State) {
		try {
			String jsonState = new String(Base64.getUrlDecoder().decode(base64State), StandardCharsets.UTF_8);
			return objectMapper.readValue(jsonState, Map.class);
		} catch (Exception e) {
			throw new RuntimeException("Twitter Oauth decode 실패", e);
		}
	}
}