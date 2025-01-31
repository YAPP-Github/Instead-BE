package org.mainapplication.global.oauth2.handler;

import java.io.IOException;

import org.mainapplication.global.constants.UrlConstants;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws
		IOException, ServletException {

		//TODO 서비스 페이지로 리다이렉트할지, Error Response를 리턴할지 추후에 결정
		response.sendRedirect(UrlConstants.PROD_DOMAIN_URL + "?error=" + exception.getMessage());
	}
}