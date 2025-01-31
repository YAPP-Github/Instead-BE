package org.uploadscheduleapplication.util.dto;

import java.time.LocalDateTime;

public record TimeRange(
	LocalDateTime startTime,
	LocalDateTime endTime
) {
	public static TimeRange ofTimes(LocalDateTime startTime, LocalDateTime endTime) {
		return new TimeRange(startTime, endTime);
	}
}
