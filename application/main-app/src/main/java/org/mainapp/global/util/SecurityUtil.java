package org.mainapp.global.util;

import org.mainapp.domain.v1.auth.exception.AuthErrorCode;
import org.mainapp.global.error.CustomException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
	public static Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		try {
			return Long.parseLong(authentication.getName());
		} catch (Exception e) {
			throw new CustomException(AuthErrorCode.AUTH_NOT_FOUND);
		}
	}
}
