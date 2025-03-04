package org.mainapp.domain.sns.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TwitterOauthUtil {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	// Base64 데이터를 JSON으로 디코딩
	public static Map<String, String> decodeStateFromBase64(String base64State) {
		try {
			String jsonState = new String(Base64.getUrlDecoder().decode(base64State), StandardCharsets.UTF_8);
			return objectMapper.readValue(jsonState, Map.class);
		} catch (Exception e) {
			throw new RuntimeException("Twitter Oauth decode 실패", e);
		}
	}
}