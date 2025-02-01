package org.mainapplication.domain.post.controller.request;

import java.time.LocalDateTime;
import java.util.List;

import org.domainmodule.post.entity.type.PostStatusType;
import org.mainapplication.domain.post.controller.request.type.UpdatePostType;
import org.springframework.lang.Nullable;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdatePostBasicRequest {

	@NotNull(message = "게시물 수정 타입을 지정해주세요.")
	private UpdatePostType updateType;

	@Nullable
	private PostStatusType status;

	@Nullable
	private LocalDateTime uploadTime;

	@Nullable
	private String content;

	@Nullable
	private List<String> imageUrls;
}
