package org.mainapplication.domain.post.controller.request;

import java.util.List;

import org.mainapplication.domain.post.controller.request.type.UpdatePostsBasicRequestItem;
import org.mainapplication.domain.post.controller.request.type.UpdatePostsType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "게시물 일괄 일반 수정 API 요청 본문")
public class UpdatePostsBasicRequest {

	@Schema(description = "게시물 수정 정보 리스트")
	@NotNull(message = "게시물 수정 정보를 입력해주세요.")
	private List<UpdatePostsBasicRequestItem> posts;

	@Schema(description = "게시물 수정 타입에 대한 Enum (상태 수정, 업로드 예약일시 수정)", example = "UPLOAD_TIME")
	@NotNull(message = "게시물 수정 타입을 지정해주세요.")
	private UpdatePostsType updateType;
}
