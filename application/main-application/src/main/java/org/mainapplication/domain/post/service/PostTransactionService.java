package org.mainapplication.domain.post.service;

import java.util.List;

import org.domainmodule.post.entity.Post;
import org.domainmodule.post.repository.PostRepository;
import org.domainmodule.postgroup.entity.PostGroup;
import org.domainmodule.postgroup.repository.PostGroupRepository;
import org.mainapplication.domain.post.service.dto.SavePostGroupAndPostDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostTransactionService {

	private final PostGroupRepository postGroupRepository;
	private final PostRepository postRepository;

	/**
	 * Post 리스트를 DB에 저장하는 메서드
	 */
	@Transactional
	public List<Post> savePosts(List<Post> posts) {
		return posts.stream()
			.map(postRepository::save)
			.toList();
	}

	/**
	 * PostGroup과 해당 PostGroup에서 생성된 Post 리스트를 DB에 저장하는 메서드
	 */
	@Transactional
	public SavePostGroupAndPostDto savePostGroupAndPosts(PostGroup postGroup, List<Post> posts) {
		PostGroup savedPostGroup = postGroupRepository.save(postGroup);
		List<Post> savedPosts = savePosts(posts);
		return new SavePostGroupAndPostDto(savedPostGroup, savedPosts);
	}
}
