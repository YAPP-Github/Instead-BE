package org.mainapplication.domain.post.controller;

import org.mainapplication.domain.post.controller.request.CreatePostsRequest;
import org.mainapplication.domain.post.controller.response.CreatePostsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/agents/{agentId}/post-groups")
@Tag(name = "Post API", description = "게시물에 대한 요청을 처리하는 API입니다.")
public class PostController {

	@Operation(summary = "게시물 그룹 및 게시물 생성 API", description = "에이전트에 새 게시물 그룹을 추가하고 게시물을 생성합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "게시물 그룹 및 게시물 생성 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청 형식"),
		@ApiResponse(responseCode = "404", description = "에이전트 찾을 수 없음")
	})
	@PostMapping("/posts")
	public ResponseEntity<CreatePostsResponse> createPosts(
		@PathVariable Long agentId,
		@RequestParam(defaultValue = "5") Integer limit,
		@RequestBody CreatePostsRequest createPostsRequest
	) {
		return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
		// 참고 X
		// 뉴스 참고
		// 이미지 참고
	}
}
