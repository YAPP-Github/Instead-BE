package org.mainapplication.domain.post.service;

import java.util.List;

import org.domainmodule.post.entity.Post;
import org.domainmodule.post.repository.PostRepository;
import org.domainmodule.postgroup.entity.PostGroup;
import org.domainmodule.postgroup.entity.PostGroupImage;
import org.domainmodule.postgroup.entity.PostGroupRssCursor;
import org.domainmodule.postgroup.repository.PostGroupImageRepository;
import org.domainmodule.postgroup.repository.PostGroupRepository;
import org.domainmodule.postgroup.repository.PostGroupRssCursorRepository;
import org.mainapplication.domain.post.service.dto.SavePostGroupAndPostsDto;
import org.mainapplication.domain.post.service.dto.SavePostGroupWithImagesAndPostsDto;
import org.mainapplication.domain.post.service.dto.SavePostGroupWithRssCursorAndPostsDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostTransactionService {

	private final PostGroupRepository postGroupRepository;
	private final PostRepository postRepository;
	private final PostGroupRssCursorRepository postGroupRssCursorRepository;
	private final PostGroupImageRepository postGroupImageRepository;

	/**
	 * 생성된 Post 엔티티 리스트를 DB에 저장하는 메서드
	 */
	@Transactional
	public List<Post> savePosts(List<Post> posts) {
		return posts.stream()
			.map(postRepository::save)
			.toList();
	}

	/**
	 * 생성된 PostGroup 엔티티를 DB에 저장하는 메서드
	 */
	@Transactional
	public PostGroup savePostGroup(PostGroup postGroup) {
		return postGroupRepository.save(postGroup);
	}

	/**
	 * 생성된 PostGroupRssCursor 엔티티를 DB에 저장하는 메서드
	 */
	@Transactional
	public PostGroupRssCursor savePostGroupRssCursor(PostGroupRssCursor postGroupRssCursor) {
		return postGroupRssCursorRepository.save(postGroupRssCursor);
	}

	/**
	 * 생성된 PostGroupImage 엔티티를 DB에 저장하는 메서드
	 */
	@Transactional
	public List<PostGroupImage> savePostGroupImages(List<PostGroupImage> postGroupImages) {
		if (postGroupImages == null) {
			return null;
		}
		return postGroupImages.stream()
			.map(postGroupImageRepository::save)
			.toList();
	}

	/**
	 * PostGroup과 해당 PostGroup에서 생성된 Post 리스트를 DB에 저장하는 메서드
	 */
	@Transactional
	public SavePostGroupAndPostsDto savePostGroupAndPosts(PostGroup postGroup, List<Post> posts) {
		PostGroup savedPostGroup = savePostGroup(postGroup);
		List<Post> savedPosts = savePosts(posts);
		return new SavePostGroupAndPostsDto(savedPostGroup, savedPosts);
	}

	/**
	 * PostGroup과 PostGroupRssCursor, 그리고 해당 PostGroup에서 생성된 Post 리스트를 DB에 저장하는 메서드
	 */
	@Transactional
	public SavePostGroupWithRssCursorAndPostsDto savePostGroupWithRssCursorAndPosts(
		PostGroup postGroup,
		PostGroupRssCursor postGroupRssCursor,
		List<Post> posts
	) {
		PostGroup savedPostGroup = savePostGroup(postGroup);
		PostGroupRssCursor savedPostGroupRssCursor = savePostGroupRssCursor(postGroupRssCursor);
		List<Post> savedPosts = savePosts(posts);
		return new SavePostGroupWithRssCursorAndPostsDto(savedPostGroup, savedPostGroupRssCursor, savedPosts);
	}

	/**
	 * PostGroup과 PostGroupImage 리스트, 그리고 해당 PostGroup에서 생성된 Post 리스트를 DB에 저장하는 메서드
	 */
	@Transactional
	public SavePostGroupWithImagesAndPostsDto savePostGroupWithImagesAndPosts(
		PostGroup postGroup,
		List<PostGroupImage> postGroupImages,
		List<Post> posts
	) {
		PostGroup savedPostGroup = savePostGroup(postGroup);
		List<PostGroupImage> savedPostGroupImages = savePostGroupImages(postGroupImages);
		List<Post> savedPosts = savePosts(posts);
		return new SavePostGroupWithImagesAndPostsDto(savedPostGroup, savedPostGroupImages, savedPosts);
	}
}
