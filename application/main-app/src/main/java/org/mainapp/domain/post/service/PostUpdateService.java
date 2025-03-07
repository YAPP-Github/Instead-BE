package org.mainapp.domain.post.service;

import java.util.List;

import org.domainmodule.post.entity.Post;
import org.domainmodule.post.entity.PostImage;
import org.domainmodule.post.entity.type.PostStatusType;
import org.domainmodule.post.repository.PostRepository;
import org.mainapp.domain.post.controller.request.UpdatePostContentRequest;
import org.mainapp.domain.post.controller.request.UpdatePostsMetadataRequest;
import org.mainapp.domain.post.controller.request.UpdateReservedPostsRequest;
import org.mainapp.domain.post.exception.PostErrorCode;
import org.mainapp.global.error.CustomException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostUpdateService {

	private final PostTransactionService postTransactionService;
	private final PostRepository postRepository;

	/**
	 * 게시물 내용 수정 메서드. updateType에 따라 분기
	 */
	public void updatePostContent(Post post, UpdatePostContentRequest request) {
		// content 필드 검증 및 Post 엔티티 수정
		if (request.getContent() == null) {
			throw new CustomException(PostErrorCode.INVALID_UPDATING_POST_TYPE);
		}
		postTransactionService.updatePostContent(post, request.getContent());

		// 수정 타입에 따라 분기
		switch (request.getUpdateType()) {
			case CONTENT -> postTransactionService.updatePostContent(post, request.getContent());
			case CONTENT_IMAGE -> updatePostImages(post, request);
		}
	}

	/**
	 * 게시물의 내용 및 이미지 수정 메서드.
	 */
	private void updatePostImages(Post post, UpdatePostContentRequest request) {
		// 수정 타입 검증
		if (request.getContent() == null || request.getImageUrls() == null) {
			throw new CustomException(PostErrorCode.INVALID_UPDATING_POST_TYPE);
		}

		// Post 엔티티 내 PostImage 엔티티 리스트 조회
		List<String> savedImageUrls = post.getPostImages().stream()
			.map(PostImage::getUrl)
			.toList();

		// 요청에만 존재하는 PostImage 엔티티 생성
		List<PostImage> newPostImages = request.getImageUrls().stream()
			.filter(imageUrl -> !savedImageUrls.contains(imageUrl))
			.map(newImageUrl -> PostImage.create(post, newImageUrl))
			.toList();
		postTransactionService.savePostImages(newPostImages);

		// DB에만 존재하는 PostImage 엔티티 제거
		List<PostImage> removedPostImages = post.getPostImages().stream()
			.filter(postImage -> !request.getImageUrls().contains(postImage.getUrl()))
			.toList();
		postTransactionService.deletePostImages(removedPostImages);
	}

	/**
	 * 게시물 기타 정보 수정 메서드.
	 */
	public void updatePostsMetadata(List<Post> posts, UpdatePostsMetadataRequest request) {
		// Post 엔티티 리스트 수정
		request.getPosts()
			.forEach(postRequest -> {
				Post post = postRepository.findById(postRequest.getPostId())  // 1차 캐시 조회
					.orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

				if (postRequest.getStatus() != null) {
					post.updateStatus(postRequest.getStatus());
				}
				if (postRequest.getUploadTime() != null) {
					// 업로드 예약일시가 변경되는 경우는 업로드 예약 상태가 되는 경우만 존재
					post.updateStatus(PostStatusType.UPLOAD_RESERVED);
					post.updateUploadTime(postRequest.getUploadTime());
				}
				if (postRequest.getDisplayOrder() != null) {
					post.updateDisplayOrder(postRequest.getDisplayOrder());
				}
				post.markPreventUpdatedAt(); // updateAt 갱신 방지

			});

		// 수정 내용 저장
		postTransactionService.savePosts(posts);
	}

	/**
	 * 계정별 예약 게시물 예약일시 수정 메서드.
	 */
	public void updateReservedPostsUploadTime(List<Post> posts, UpdateReservedPostsRequest request) {
		// Post 엔티티 리스트 수정
		request.posts()
			.forEach(postRequest -> {
				Post post = postRepository.findById(postRequest.postId())  // 1차 캐시 조회
					.orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

				post.updateStatus(PostStatusType.UPLOAD_RESERVED);
				post.updateUploadTime(postRequest.uploadTime());
			});
		// 수정 내용 저장
		postTransactionService.savePosts(posts);
	}
}
