package com.aethertrack.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.aethertrack")
@EntityScan(basePackages = "com.aethertrack.core.domain")
@EnableJpaRepositories(basePackages = "com.aethertrack")
@EnableAsync
@EnableScheduling
public class AetherTrackApplication {

  public static void main(String[] args) {
    SpringApplication.run(AetherTrackApplication.class, args);
  }
}
