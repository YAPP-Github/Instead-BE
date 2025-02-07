package org.mainapplication.domain.post.controller.request;

import java.util.List;

import org.mainapplication.domain.post.controller.request.type.UpdatePostsRequestItem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "게시물 기타 정보 수정 API 요청 본문")
public class UpdatePostsRequest {

	@Schema(description = "게시물 수정 정보 리스트")
	@NotNull(message = "게시물 수정 정보를 입력해주세요.")
	@Valid
	private List<UpdatePostsRequestItem> posts;
}
