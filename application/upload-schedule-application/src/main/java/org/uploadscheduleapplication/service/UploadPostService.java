package org.uploadscheduleapplication.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.domainmodule.post.entity.Post;
import org.domainmodule.post.entity.type.PostStatusType;
import org.hibernate.Internal;
import org.snsclient.twitter.dto.response.TwitterToken;
import org.snsclient.twitter.service.TwitterApiService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.uploadscheduleapplication.post.PostService;
import org.uploadscheduleapplication.snstoken.SnsTokenService;
import org.uploadscheduleapplication.util.dto.UploadPostDto;
import org.uploadscheduleapplication.util.mapper.SnsTokenMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import twitter4j.Twitter;
import twitter4j.TwitterException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadPostService {
	private final TwitterApiService twitterApiService;
	private final PostService postService;
	private final SnsTokenService snsTokenService;

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
			log.error("Post ID: {} 업로드 실패 (최대 재시도 횟수 초과)", uploadPostDto.post().getId());
			return CompletableFuture.completedFuture(null);
		}

		try {
			Long tweetId = twitterApiService.postTweet(
				uploadPostDto.snsToken().getAccessToken(),
				uploadPostDto.post().getContent()
			);
			handleUploadSuccess(uploadPostDto, tweetId);
			return CompletableFuture.completedFuture(null);
		} catch (Exception ex) {
			handleUploadError(uploadPostDto, ex, retryCount);
			return CompletableFuture.failedFuture(ex);
		}
	}

	public void handleUploadSuccess(UploadPostDto uploadPostDto, Long tweetId) {
		postService.updatePostStatus(uploadPostDto.post(), PostStatusType.UPLOADED);
		log.info("Tweet 업로드 성공 Post ID: {}, Tweet ID: {}", uploadPostDto.post().getId(), tweetId);
	}

	private void handleUploadError(UploadPostDto uploadPostDto, Throwable exception, int retryCount) {
		if (exception instanceof TwitterException twitterException) {
			int statusCode = twitterException.getStatusCode();
			log.error("TwitterException 발생. 상태 코드: {}, 메시지: {}", statusCode, twitterException.getMessage());

			switch (statusCode) {
				case 401 -> handleUnauthorizedError(uploadPostDto, retryCount);
				case 403 -> handleForbiddenError(uploadPostDto);
				case 429 -> handleRateLimitError(uploadPostDto);
				case 500, 503 -> handleServerError(uploadPostDto);
				default -> handleUnknownError(twitterException);
			}
		}
	}

	//TODO 예외처리 부분 따로 파일로 관리
	//401 Unauthorized: 토큰을 재발급 후 재시도
	private void handleUnauthorizedError(UploadPostDto uploadPostDto, int retryCount) {
		log.warn("권한 부족: {}. Twitter 계정에 대한 권한이 없습니다.", uploadPostDto.post().getId());
		UploadPostDto newTokens = snsTokenService.reissueToken(uploadPostDto);
		processUploadPost(newTokens, retryCount + 1);
	}

	// 403 Forbidden: 권한 부족으로 업로드 불가
	private void handleForbiddenError(UploadPostDto uploadPostDto) {
		log.warn("업로드 불가: Post ID: {}. Twitter 계정에 대한 접근 권한이 없습니다.", uploadPostDto.post().getId());
	}

	// 429 Too Many Requests: 속도 제한 초과
	private void handleRateLimitError(UploadPostDto uploadPostDto) {
		log.warn("속도 제한 초과: Post ID: {}. 잠시 후 다시 시도하세요.", uploadPostDto.post().getId());
	}

	// 500, 503 Internal Server Error: Twitter 서버 오류
	private void handleServerError(UploadPostDto uploadPostDto) {
		log.warn("Twitter 서버 오류 발생: Post ID: {}. 재시도 가능", uploadPostDto.post().getId());
	}

	// 기타 예외 처리
	private void handleUnknownError(Throwable exception) {
		log.error("알 수 없는 업로드 실패: {}", exception.getMessage(), exception);
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
