package org.uploadscheduleapplication.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.domainmodule.post.entity.Post;
import org.snsclient.twitter.service.TwitterApiService;
import org.springframework.stereotype.Service;
import org.uploadscheduleapplication.post.PostService;
import org.uploadscheduleapplication.util.dto.UploadPostDto;
import org.uploadscheduleapplication.util.mapper.SnsTokenMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadPostService {
	private final TwitterApiService twitterApiService;
	private final PostService postService;

	public void uploadPosts() {

		try {
			// 업로드할 Post DTO 리스트 가져오기
			List<Post> postsReadyForUpload = postService.getPostsReadyForUpload();
			List<UploadPostDto> uploadPostInfoList = getInfoReadyForUpload(postsReadyForUpload);
			// 비동기 작업 처리
			uploadPostInfoList.forEach(this::processUploadPost);

		} catch (Exception e) {
			log.error("Post 업로드 스케쥴링 에러 {}", e.getMessage(), e);
		}
	}

	/**
	 * 단일 Post를 비동기로 처리
	 *
	 * @param uploadPostDto 업로드할 Post DTO
	 */
	private void processUploadPost(UploadPostDto uploadPostDto) {
		CompletableFuture<Long> future = twitterApiService.postTweetAsync(
			SnsTokenMapper.toTwitterToken(uploadPostDto.snsToken()),
			uploadPostDto.post().getContent()
		);
		handleAsyncResponse(uploadPostDto, future);
	}

	/**
	 * 비동기 요청 결과를 처리
	 *
	 * @param uploadPostDto 업로드할 Post DTO
	 * @param future 비동기 작업 결과
	 */
	private void handleAsyncResponse(UploadPostDto uploadPostDto, CompletableFuture<Long> future) {
		future.whenComplete((tweetId, exception) -> {
			if (exception != null) {
				log.error("Tweet 업로드에 실패했습니다. Post ID: {}. Error: {}", uploadPostDto.post().getId(), exception.getMessage());
			} else {
				log.info("Tweet 업로드에 성공했습니다. Post ID: {}, Tweet ID: {}", uploadPostDto.post().getId(), tweetId);
			}
		});
	}

	/**
	 * SnsToken과 Post만을 가지는 DTO로 변환
	 */
	private List<UploadPostDto> getInfoReadyForUpload(List<Post> posts) {
		return posts.stream()
			.map(UploadPostDto::fromPost)
			.toList();
	}
}
