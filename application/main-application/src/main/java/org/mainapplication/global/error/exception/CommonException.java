package org.mainapplication.global.error.exception;

import org.mainapplication.global.error.CustomException;
import org.mainapplication.global.error.ErrorCodeStatus;

public class CommonException extends CustomException {
	public CommonException(ErrorCodeStatus errorCodeStatus) {
		super(errorCodeStatus);
	}
}
