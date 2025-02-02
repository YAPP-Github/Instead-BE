package org.mainapplication.domain.post.controller.response;

import java.util.List;

import org.mainapplication.domain.post.controller.response.type.PostResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "게시물 그룹 및 게시물 생성 API 응답 본문")
public class CreatePostsResponse {

	@Schema(description = "생성된 게시물 그룹 id", example = "1")
	private Long postGroupId;

	@Schema(description = "뉴스를 참고해 생성할 경우, 현재 피드에 남은 뉴스가 있는지 여부", example = "true")
	private Boolean eof;

	@Schema(description = "생성된 게시물 리스트")
	private List<PostResponse> posts;
}
