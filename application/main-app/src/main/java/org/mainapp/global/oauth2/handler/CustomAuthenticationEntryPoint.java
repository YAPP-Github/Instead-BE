package org.mainapp.global.oauth2.handler;

import org.mainapp.global.error.ErrorResponse;
import org.mainapp.global.response.GlobalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	public AuthenticationEntryPoint oAuth2EntryPoint() {
		return (request, response, authException) -> {
			response.setContentType("application/json;charset=UTF-8");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

			ErrorResponse errorResponse = ErrorResponse.of(
				authException.getClass().getSimpleName(),
				"로그인이 필요합니다."
			);

			GlobalResponse globalResponse = GlobalResponse.fail(
				HttpStatus.UNAUTHORIZED.value(),
				errorResponse
			);

			// JSON 변환 후 응답
			String jsonResponse = objectMapper.writeValueAsString(globalResponse);
			response.getWriter().write(jsonResponse);
		};
	}
}