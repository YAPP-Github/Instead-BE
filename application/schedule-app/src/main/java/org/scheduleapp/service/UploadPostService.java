package org.scheduleapp.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.domainmodule.post.entity.Post;
import org.snsclient.twitter.facade.TwitterApiService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.scheduleapp.exception.TwitterUploadExceptionHandler;
import org.scheduleapp.post.PostService;
import org.scheduleapp.snstoken.SnsTokenService;
import org.scheduleapp.util.dto.UploadPostDto;

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
	private final TwitterUploadExceptionHandler uploadExceptionHandler;

	/**
	 * post의 upload_time을 확인하여 sns 게시물 업로드
	 */
	public void uploadPosts() {

		try {
			// 업로드할 Post DTO 리스트 가져오기
			List<Post> postsReadyForUpload = postService.getPostsReadyForUpload();
			List<UploadPostDto> uploadPostInfoList = getInfoReadyForUpload(postsReadyForUpload);
			List<UploadPostDto> retryList = Collections.synchronizedList(new ArrayList<>());

			// CompletableFuture 리스트 생성
			List<CompletableFuture<Void>> futures = uploadPostInfoList.stream()
				.map(post -> processUploadPost(post, retryList))
				.toList();

			// 모든 업로드가 완료될 때까지 대기
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

			// 토큰 재발급 후 2번째 시도
			if (!retryList.isEmpty()) {
				List<CompletableFuture<Void>> retryFutures = retryList.stream()
					.map(this::reUploadWithNewToken)
					.toList();

				CompletableFuture.allOf(retryFutures.toArray(new CompletableFuture[0])).join();
			}
		} catch (Exception e) {
			log.error("Post 업로드 스케쥴링 에러 {}", e.getMessage(), e);
		}
	}

	/**
	 * @Async 비동기 업로드 수행
	 */
	@Async("threadPoolTaskExecutor")
	public CompletableFuture<Void> processUploadPost(UploadPostDto uploadPostDto, List<UploadPostDto> retryList) {
		return CompletableFuture.runAsync(() -> {
			try {
				// 업로드 할 이미지 mediaID 리스트
				String[] mediaIds = getMediaIds(uploadPostDto);

				// tweet 올리기
				String tweetId = twitterApiService.postTweet(
					uploadPostDto.snsToken().getAccessToken(),
					uploadPostDto.post().getContent(),
					mediaIds
				);

				uploadExceptionHandler.handleUploadSuccess(uploadPostDto, tweetId);
			} catch (Exception e) {
				if (e instanceof TwitterException te && te.getStatusCode() == 401) {
					log.warn("401 오류, 재시도 대상으로 추가 - Post ID: {}", uploadPostDto.post().getId());
					retryList.add(uploadPostDto);
				} else {
					uploadExceptionHandler.handleUploadError(uploadPostDto, e);
				}
			}
		});
	}

	@Async("threadPoolTaskExecutor")
	public CompletableFuture<Void> reUploadWithNewToken(UploadPostDto oldDto) {
		return CompletableFuture.runAsync(() -> {
			try {
				UploadPostDto newDto = snsTokenService.reissueToken(oldDto);

				String[] mediaIds = getMediaIds(newDto);

				String tweetId = twitterApiService.postTweet(
					newDto.snsToken().getAccessToken(),
					newDto.post().getContent(),
					mediaIds
				);

				uploadExceptionHandler.handleUploadSuccess(newDto, tweetId);
			} catch (Exception e) {
				log.error("재시도 실패 - Post ID: {}, 예외: {}", oldDto.post().getId(), e.getMessage());
				uploadExceptionHandler.handleUploadError(oldDto, e);
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

	/**
	 * 이미지 업로드 요청 후 mediaId의 값 받아오기
	 */
	private String[] getMediaIds(UploadPostDto uploadPostDto) {
		List<String> imageUrls = postService.getPostImageUrlsByPost(uploadPostDto.post());

		if (imageUrls.isEmpty()) {
			return null;
		}
		// 업로드 할 이미지 mediaID 리스트
		return imageUrls.stream()
				.map(url -> uploadImageWithUrl(url, uploadPostDto.snsToken().getAccessToken()))
				.toArray(String[]::new);
	}

	private String uploadImageWithUrl(String url, String accessToken) {
		try {
			return twitterApiService.uploadMedia(url,accessToken);
		} catch (TwitterException e) {
			throw new RuntimeException("Twitter media 업로드 실패 URL : " + url, e);
		}
	}
}
