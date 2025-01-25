package org.uploadscheduleapplication.util;

import java.time.LocalDateTime;

import org.uploadscheduleapplication.util.dto.TimeRange;

public class TimeUtil {
	/**
	 * 현재 시간에서 같은 분(minute)에 해당하는 시작 시간(startTime)과 종료 시간(lastTime)을 리턴
	 * @param currentTime
	 * @return
	 */
	public static TimeRange getStartAndEndTimeRange(LocalDateTime currentTime) {
		LocalDateTime startTime = currentTime.withSecond(0).withNano(0); // 현재 분의 0초
		LocalDateTime lastTime = currentTime.withSecond(59).withNano(999_999_999); // 현재 분의 59초
		return TimeRange.ofTimes(startTime, lastTime);
	}
}
