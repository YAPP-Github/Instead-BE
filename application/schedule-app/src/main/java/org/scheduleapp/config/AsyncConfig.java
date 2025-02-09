package org.scheduleapp.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

	@Bean(name = "threadPoolTaskExecutor")
	public Executor threadPoolTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2); // 기본적으로 유지할 최소 스레드 수
		executor.setMaxPoolSize(5); // 최대 스레드 수
		executor.setQueueCapacity(5); // 작업 큐의 용량을 5로설정합니다. 큐가 가득 차면 새로운 작업은 거부
		executor.setThreadNamePrefix("Async-");
		executor.initialize();
		return executor;
	}
}
