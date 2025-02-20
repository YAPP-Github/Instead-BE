package org.mainapp.domain.post.controller.request;

import java.util.List;

import org.mainapp.domain.post.controller.request.type.UpdateReservedPostsRequestItem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(description = "계정별 예약 게시물 예약일시 수정 API 요청 본문")
public record UpdateReservedPostsRequest(
	@Schema(description = "게시물 예약일시 수정 정보 리스트")
	@NotNull(message = "게시물 예약일시 수정 정보를 입력해주세요.")
	@Valid
	List<UpdateReservedPostsRequestItem> posts
) {
}
