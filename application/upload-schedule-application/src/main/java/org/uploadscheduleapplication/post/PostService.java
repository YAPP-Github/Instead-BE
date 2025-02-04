package org.uploadscheduleapplication.post;

import java.time.LocalDateTime;
import java.util.List;

import org.domainmodule.post.entity.Post;
import org.domainmodule.post.entity.type.PostStatusType;
import org.domainmodule.post.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uploadscheduleapplication.util.TimeUtil;
import org.uploadscheduleapplication.util.dto.TimeRange;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
	private final PostRepository postRepository;

	/**
	 * @return 현재 시간의 분단위로 시간이 같은 Post들 반환
	 */
	//TODO DTO로 조회해서 가져올지 고민 후 리펙토링 필요
	public List<Post> getPostsReadyForUpload() {
		TimeRange timeRange = TimeUtil.getStartAndEndTimeRange(LocalDateTime.now());
		return postRepository.findPostsWithSnsTokenByTimeRange(timeRange.startTime(), timeRange.endTime(), PostStatusType.UPLOAD_RESERVED);
	}

	@Transactional
	public void updatePostStatus(Post post, PostStatusType postStatus) {
		post.updateStatus(postStatus);
		postRepository.save(post);
	}
}
