package org.mainapplication.global.constants;

public final class PostGenerationCount {
	private PostGenerationCount() {
		throw new UnsupportedOperationException("인스턴스를 생성할 수 없습니다.");
	}

	// 한 번의 게시물 생성 시 생성되는 게시물 수, PostController에서 사용
	public static final String POST_GENERATION_POST_COUNT = "5";
	// 게시물 그룹 당 최대 게시물 생성 가능 횟수
	public static final Integer MAX_POST_GENERATION_COUNT = 5;
}
