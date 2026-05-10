package com.aethertrack.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async executor configuration for notification fan-out and non-blocking tasks.
 *
 * <p>Can be replaced with a Java 26 virtual-thread executor once the runtime image is aligned.
 */
@Configuration
public class AsyncConfig {

  @Bean(name = "applicationTaskExecutor")
  public Executor applicationTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(8);
    executor.setMaxPoolSize(32);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("aethertrack-");
    executor.initialize();
    return executor;
  }
}
