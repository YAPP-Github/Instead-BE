package org.mainapp.global.util;

import org.mainapp.domain.auth.exception.AuthErrorCode;
import org.mainapp.global.error.CustomException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {
	public static String getCurrentMemberId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		try {
			return authentication.getName();
		} catch (Exception e) {
			throw new CustomException(AuthErrorCode.AUTH_NOT_FOUND);
		}
	}
}
