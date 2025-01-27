package org.uploadscheduleapplication.postgroup;

import java.util.List;

import org.domainmodule.post.entity.Post;
import org.domainmodule.postgroup.entity.PostGroup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostGroupService {
	/**
	 * 업로드 할 Post에서 PostGroup 리스트를 추출
	 *
	 * @return PostGroup 리스트
	 */
	@Transactional(readOnly = true)
	public List<PostGroup> getPostGroupsForReadyPosts(List<Post> postsReadyForUpload) {
		return postsReadyForUpload.stream()
			.map(Post::getPostGroup)
			.distinct()
			.toList();
	}

}
