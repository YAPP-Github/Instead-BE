package org.domainmodule.postgroup.exception;

import org.domainmodule.postgroup.entity.PostGroup;

public class PostGroupRssCursorNotFoundException extends RuntimeException {

	public PostGroupRssCursorNotFoundException(PostGroup postGroup) {
		super("게시물 그룹에 RSS 피드 cursor가 존재하지 않습니다. postGroupId: " + postGroup.getId());
	}
}
