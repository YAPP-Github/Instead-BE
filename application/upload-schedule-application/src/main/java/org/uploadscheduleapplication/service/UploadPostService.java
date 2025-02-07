package org.uploadscheduleapplication.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.domainmodule.post.entity.Post;
import org.snsclient.twitter.service.TwitterApiService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.uploadscheduleapplication.exception.TwitterUploadExceptionHandler;
import org.uploadscheduleapplication.post.PostService;
import org.uploadscheduleapplication.snstoken.SnsTokenService;
import org.uploadscheduleapplication.util.dto.UploadPostDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadPostService {
	private final TwitterApiService twitterApiService;
	private final PostService postService;
	private final SnsTokenService snsTokenService;
	private final TwitterUploadExceptionHandler uploadExceptionHandler;
	private static final int MAX_RETRY_COUNT = 1;

	/**
	 * post의 upload_time을 확인하여 sns 게시물 업로드
	 */
	public void uploadPosts() {

		try {
			// 업로드할 Post DTO 리스트 가져오기
			List<Post> postsReadyForUpload = postService.getPostsReadyForUpload();
			List<UploadPostDto> uploadPostInfoList = getInfoReadyForUpload(postsReadyForUpload);

			// CompletableFuture 리스트 생성
			List<CompletableFuture<Void>> futures = uploadPostInfoList.stream()
				.map(post -> processUploadPost(post, 0))
				.toList();

			// 모든 업로드가 완료될 때까지 대기
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		} catch (Exception e) {
			log.error("Post 업로드 스케쥴링 에러 {}", e.getMessage(), e);
		}
	}

	/**
	 * @Async 비동기 업로드 수행
	 */
	@Async("threadPoolTaskExecutor")
	public CompletableFuture<Void> processUploadPost(UploadPostDto uploadPostDto, int retryCount) {
		if (retryCount > MAX_RETRY_COUNT) {
			log.error("업로드 실패 (최대 재시도 횟수 초과) - Post ID: {} ", uploadPostDto.post().getId());
			return CompletableFuture.completedFuture(null);
		}
		try {
			Long tweetId = twitterApiService.postTweet(
				uploadPostDto.snsToken().getAccessToken(),
				uploadPostDto.post().getContent()
			);
			uploadExceptionHandler.handleUploadSuccess(uploadPostDto, tweetId);
			return CompletableFuture.completedFuture(null);
		} catch (Exception ex) {
			uploadExceptionHandler.handleUploadError(uploadPostDto, ex, retryCount, () -> {
				UploadPostDto newTokens = snsTokenService.reissueToken(uploadPostDto);
				processUploadPost(newTokens, retryCount + 1);
			});
			return CompletableFuture.failedFuture(ex);
		}
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
