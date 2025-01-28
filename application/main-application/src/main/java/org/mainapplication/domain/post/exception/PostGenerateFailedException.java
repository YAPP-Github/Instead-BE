package org.mainapplication.domain.post.exception;

import org.mainapplication.domain.post.exception.code.PostErrorCode;
import org.mainapplication.global.error.CustomException;

public class PostGenerateFailedException extends CustomException {

	public PostGenerateFailedException() {
		super(PostErrorCode.POST_GENERATE_FAILED);
	}
}
