package org.mainapplication.domain.post.exception;

import org.mainapplication.domain.post.exception.code.PostErrorCode;
import org.mainapplication.global.error.CustomException;

public class NewsGetFailedException extends CustomException {

	public NewsGetFailedException() {
		super(PostErrorCode.NEWS_GET_FAILED);
	}
}
