package org.mainapplication.domain.post.controller.request.type;

import java.time.LocalDateTime;

import org.domainmodule.post.entity.type.PostStatusType;
import org.springframework.lang.Nullable;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdatePostsRequestItem {

	@NotNull(message = "게시물 id를 지정해주세요.")
	private Long postId;

	@Nullable
	private PostStatusType status;

	@Nullable
	private LocalDateTime uploadTime;
}
