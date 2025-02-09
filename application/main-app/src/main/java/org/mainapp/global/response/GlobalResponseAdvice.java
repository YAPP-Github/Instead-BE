package org.mainapp.global.response;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import jakarta.servlet.http.HttpServletResponse;

@RestControllerAdvice(basePackages = "org.mainapplication")
public class GlobalResponseAdvice implements ResponseBodyAdvice {

	@Override
	public boolean supports(MethodParameter returnType, Class converterType) {
		return true;
	}

	@Override
	public Object beforeBodyWrite(
		Object body,
		MethodParameter returnType,
		MediaType selectedContentType,
		Class selectedConverterType,
		ServerHttpRequest request,
		ServerHttpResponse response) {

		HttpServletResponse servletResponse =
			((ServletServerHttpResponse)response).getServletResponse();

		// http 상태 코드
		int status = servletResponse.getStatus();
		HttpStatus resolve = HttpStatus.resolve(status);

		// 상태 코드가 null이거나 응답 바디가 문자열인 경우 원본 응답을 반환
		if (resolve == null || body instanceof String) {
			return body;
		}

		// 2xx 범위인 경우 응답 처리
		if (resolve.series() == HttpStatus.Series.SUCCESSFUL) {
			return GlobalResponse.success(status, body);
		}
		return body;
	}
}
