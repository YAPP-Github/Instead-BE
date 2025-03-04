package org.snsclient.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TwitterOauthUtil {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	// JSON 데이터를 Base64 형식으로 인코딩
	public static String encodeStateToBase64(String userId, String clientId) {
		try {
			String jsonState = objectMapper.writeValueAsString(Map.of("userId", userId, "clientId", clientId));
			return Base64.getUrlEncoder().encodeToString(jsonState.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new RuntimeException("Twitter Oauth encode 실패", e);
		}
	}
}