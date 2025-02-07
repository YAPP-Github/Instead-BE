package org.uploadscheduleapplication.exception;

import org.domainmodule.post.entity.type.PostStatusType;
import org.springframework.stereotype.Component;
import org.uploadscheduleapplication.post.PostService;
import org.uploadscheduleapplication.util.dto.UploadPostDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import twitter4j.TwitterException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TwitterUploadExceptionHandler {

	private final PostService postService;

	public void handleUploadSuccess(UploadPostDto uploadPostDto, Long tweetId) {
		postService.updatePostStatus(uploadPostDto.post(), PostStatusType.UPLOADED);
		log.info("Tweet 업로드 성공 - Post ID: {}, Tweet ID: {}", uploadPostDto.post().getId(), tweetId);
	}

	public void handleUploadError(UploadPostDto uploadPostDto, Throwable exception, int retryCount, Runnable retryAction) {
		if (exception instanceof TwitterException twitterException) {
			int statusCode = twitterException.getStatusCode();
			switch (statusCode) {
				case 401 -> handleUnauthorizedError(uploadPostDto, retryCount, retryAction);
				case 403 -> handleForbiddenError(uploadPostDto);
				case 429 -> handleRateLimitError(uploadPostDto);
				case 500, 503 -> handleServerError(uploadPostDto);
				default -> handleUnknownError(twitterException, uploadPostDto);
			}
		}
	}

	//401 Unauthorized: 토큰을 재발급 후 재시도
	private void handleUnauthorizedError(UploadPostDto uploadPostDto, int retryCount, Runnable retryAction) {
		log.warn("권한 부족 - Post ID: {}. 토큰을 재발급 후 재시도합니다.", uploadPostDto.post().getId());
		// 전달받은 재시도 로직 실행 (토큰 갱신 후 업로드 재시도)
		retryAction.run();
	}

	// 403 Forbidden: 권한 부족으로 업로드 불가
	private void handleForbiddenError(UploadPostDto uploadPostDto) {
		log.warn("업로드 불가 - Post ID: {}. Twitter 계정에 대한 접근 권한이 없습니다.", uploadPostDto.post().getId());
		postService.updatePostStatus(uploadPostDto.post(), PostStatusType.UPLOAD_FAILED);
	}

	// 429 Too Many Requests: 속도 제한 초과
	private void handleRateLimitError(UploadPostDto uploadPostDto) {
		log.warn("속도 제한 초과 - Post ID: {}. 잠시 후 다시 시도하세요.", uploadPostDto.post().getId());
		postService.updatePostStatus(uploadPostDto.post(), PostStatusType.UPLOAD_FAILED);
	}

	// 500, 503 Internal Server Error: Twitter 서버 오류
	private void handleServerError(UploadPostDto uploadPostDto) {
		log.warn("Twitter 서버 오류 발생 - Post ID: {}.", uploadPostDto.post().getId());
		postService.updatePostStatus(uploadPostDto.post(), PostStatusType.UPLOAD_FAILED);
	}

	// 기타 예외 처리
	private void handleUnknownError(Throwable exception, UploadPostDto uploadPostDto) {
		log.error("알 수 없는 업로드 실패: {}", exception.getMessage(), exception);
		postService.updatePostStatus(uploadPostDto.post(), PostStatusType.UPLOAD_FAILED);
	}
}
