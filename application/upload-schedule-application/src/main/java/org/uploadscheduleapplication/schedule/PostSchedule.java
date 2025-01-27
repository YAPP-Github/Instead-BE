package org.uploadscheduleapplication.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.uploadscheduleapplication.service.UploadPostService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostSchedule {
	private final UploadPostService uploadPostService;
	/**
	 * 1분마다 실행되는 스케줄러
	 */
	//TODO SnsToken의 만료시간 계산 후 재발급 로직 추가해야함
	@Scheduled(cron = "0 */1 * * * *")
	public void startUploadPost() {
		uploadPostService.uploadPosts();
	}
}
