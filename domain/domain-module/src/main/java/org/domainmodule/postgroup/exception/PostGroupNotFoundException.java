package org.domainmodule.postgroup.exception;

public class PostGroupNotFoundException extends RuntimeException {

	public PostGroupNotFoundException(Long postGroupId) {
		super("게시물 그룹이 존재하지 않습니다. id: " + postGroupId);
	}
}
