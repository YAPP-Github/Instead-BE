package org.scheduleapp.schedule;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.domainmodule.post.entity.Post;
import org.snsclient.twitter.service.TwitterApiService;
import org.snsclient.twitter.service.TwitterMediaUploadService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.scheduleapp.exception.TwitterUploadExceptionHandler;
import org.scheduleapp.post.PostService;
import org.scheduleapp.snstoken.SnsTokenService;
import org.scheduleapp.util.dto.UploadPostDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadPostService {
	private final TwitterApiService twitterApiService;
	private final TwitterMediaUploadService twitterMediaUploadService;
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
			List<String> imageUrls = postService.getPostImageUrlsByPost(uploadPostDto.post());

			// 업로드 할 이미지 mediaID 리스트
			Long[] mediaIds = imageUrls.isEmpty() ? null :
				imageUrls.stream()
				.map(url -> twitterMediaUploadService.uploadMedia(url, uploadPostDto.snsToken().getAccessToken()))
				.map(Long::parseLong)
				.toArray(Long[]::new);

			Long tweetId = twitterApiService.postTweet(
				uploadPostDto.snsToken().getAccessToken(),
				uploadPostDto.post().getContent(),
				mediaIds
			);

			uploadExceptionHandler.handleUploadSuccess(uploadPostDto, tweetId);
			return CompletableFuture.completedFuture(null);
		} catch (Exception ex) {
			uploadExceptionHandler.handleUploadError(uploadPostDto, ex, retryCount,
				() -> retryUploadWithNewToken(uploadPostDto, retryCount));
			return CompletableFuture.failedFuture(ex);
		}
	}

	/**
	 * 토큰을 갱신한 후 업로드 재시도 (401 에러 발생시)
	 */
	private void retryUploadWithNewToken(UploadPostDto uploadPostDto, int retryCount) {
		UploadPostDto newTokens = snsTokenService.reissueToken(uploadPostDto);
		processUploadPost(newTokens, retryCount + 1);
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
