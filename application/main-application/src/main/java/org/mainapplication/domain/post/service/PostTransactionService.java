package org.mainapplication.domain.post.service;

import java.time.LocalDateTime;
import java.util.List;

import org.domainmodule.post.entity.Post;
import org.domainmodule.post.entity.PostImage;
import org.domainmodule.post.entity.type.PostStatusType;
import org.domainmodule.post.repository.PostImageRepository;
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
	private final PostGroupRssCursorRepository postGroupRssCursorRepository;
	private final PostGroupImageRepository postGroupImageRepository;
	private final PostRepository postRepository;
	private final PostImageRepository postImageRepository;

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

	/**
	 * 게시물의 상태를 수정하는 메서드
	 */
	@Transactional
	public void updatePostStatus(Post post, PostStatusType status) {
		post.updateStatus(status);
	}

	/**
	 * 게시물의 업로드 예약 일시를 수정하는 메서드
	 */
	@Transactional
	public void updatePostUploadTime(Post post, LocalDateTime uploadTime) {
		post.updateUploadTime(uploadTime);
	}

	/**
	 * 게시물의 내용을 수정하는 메서드
	 */
	@Transactional
	public void updatePostContent(Post post, String content) {
		post.updateContent(content);
	}

	/**
	 * Post를 단건 삭제하는 메서드
	 */
	@Transactional
	public void deletePost(Post post) {
		postRepository.delete(post);
	}

	/**
	 * Post 리스트를 삭제하는 메서드
	 */
	@Transactional
	public void deletePosts(List<Post> posts) {
		postRepository.deleteAll(posts);
	}

	/**
	 * PostImage 리스트를 DB에 저장하는 메서드
	 */
	@Transactional
	public void savePostImages(List<PostImage> postImages) {
		postImageRepository.saveAll(postImages);
	}

	/**
	 * PostImage 리스트를 삭제하는 메서드
	 */
	@Transactional
	public void deletePostImages(List<PostImage> postImages) {
		postImageRepository.deleteAll(postImages);
	}
}
