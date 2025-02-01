package org.mainapplication.domain.post.controller.request;

import java.util.List;

import org.mainapplication.domain.post.controller.request.type.UpdatePostsRequestItem;
import org.mainapplication.domain.post.controller.request.type.UpdatePostsType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdatePostsRequest {

	@NotNull(message = "게시물 수정 정보를 입력해주세요.")
	private List<UpdatePostsRequestItem> posts;

	@NotNull(message = "게시물 수정 타입을 지정해주세요.")
	private UpdatePostsType updateType;
}
