package org.uploadscheduleapplication.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.domainmodule.post.entity.Post;
import org.domainmodule.post.entity.type.PostStatus;
import org.domainmodule.snstoken.entity.SnsToken;
import org.snsclient.twitter.dto.response.TwitterToken;
import org.snsclient.twitter.service.TwitterApiService;
import org.springframework.stereotype.Service;
import org.uploadscheduleapplication.post.PostService;
import org.uploadscheduleapplication.snstoken.SnsTokenService;
import org.uploadscheduleapplication.util.dto.UploadPostDto;
import org.uploadscheduleapplication.util.mapper.SnsTokenMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import twitter4j.TwitterException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadPostService {
	private final TwitterApiService twitterApiService;
	private final PostService postService;
	private final SnsTokenService snsTokenService;

	private static final int MAX_RETRY_COUNT = 1;

	public void uploadPosts() {

		try {
			// 업로드할 Post DTO 리스트 가져오기
			List<Post> postsReadyForUpload = postService.getPostsReadyForUpload();
			List<UploadPostDto> uploadPostInfoList = getInfoReadyForUpload(postsReadyForUpload);

			// 비동기로 게시글 올리기
			uploadPostInfoList.forEach(post -> processUploadPost(post, 0));

		} catch (Exception e) {
			log.error("Post 업로드 스케쥴링 에러 {}", e.getMessage(), e);
		}
	}

	/**
	 * 단일 Post를 비동기로 처리
	 *
	 * @param uploadPostDto 업로드할 Post DTO
	 */
	private void processUploadPost(UploadPostDto uploadPostDto, int retryCount) {
		if (retryCount > MAX_RETRY_COUNT) {
			log.error("Post ID: {} 업로드 실패 (최대 재시도 횟수 초과)", uploadPostDto.post().getId());
			return;
		}

		CompletableFuture<Long> future = twitterApiService.postTweetAsync(
			SnsTokenMapper.toTwitterToken(uploadPostDto),
			uploadPostDto.post().getContent()
		);

		future.whenComplete((tweetId, exception) -> handleAsyncResponse(uploadPostDto, tweetId, exception, retryCount));
	}

	/**
	 * 비동기 요청 결과를 처리
	 */
	private void handleAsyncResponse(UploadPostDto uploadPostDto, Long tweetId, Throwable exception, int retryCount) {
		if (exception == null) {
			handleUploadSuccess(uploadPostDto, tweetId);
		} else {
			handleUploadError(uploadPostDto, exception, retryCount);
		}
	}

	private void handleUploadSuccess(UploadPostDto uploadPostDto, Long tweetId) {
		postService.updatePostStatus(uploadPostDto.post(), PostStatus.UPLOADED);
		log.info("Tweet 업로드 성공! Post ID: {}, Tweet ID: {}", uploadPostDto.post().getId(), tweetId);
	}

	private void handleUploadError(UploadPostDto uploadPostDto, Throwable exception, int retryCount) {
		if (exception instanceof TwitterException twitterException) {
			int statusCode = twitterException.getStatusCode();
			log.error("TwitterException 발생. 상태 코드: {}, 메시지: {}", statusCode, twitterException.getMessage());

			if (statusCode == 401) {
				retryWithNewToken(uploadPostDto, retryCount);
				return;
			}
		}

		log.error("알 수 없는 업로드 실패: {}", exception.getMessage(), exception);
	}

	/**
	 * 401 Unauthorized 발생 시 토큰을 재발급 후 재시도
	 */
	private void retryWithNewToken(UploadPostDto uploadPostDto, int retryCount) {
		log.warn("인증 실패: 토큰 재발급 후 재시도합니다. (재시도 {}/{})", retryCount, MAX_RETRY_COUNT);
		try {
			// 토큰 재발급
			TwitterToken newSnsToken = twitterApiService.refreshTwitterToken(uploadPostDto.snsToken().getRefreshToken());

			// Sns토큰 업데이트
			SnsToken snsToken = uploadPostDto.snsToken();
			snsTokenService.updateSnsToken(snsToken, newSnsToken);

			// 기존 Post DTO에 새로운 토큰 설정
			UploadPostDto updatedDto = UploadPostDto.from(
				uploadPostDto.post(),
				snsToken
			);

			// 재시도 실행
			processUploadPost(updatedDto, retryCount + 1);
		} catch (Exception e) {
			log.error("토큰 재발급 실패! Post ID: {}. Error: {}", uploadPostDto.post().getId(), e.getMessage(), e);
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
