package com.cmdlee.quizsushi.global.config.scheduler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class TaskExecutorConfig {
    private final ChallengeProperties challengeProperties;

    public TaskExecutorConfig(ChallengeProperties challengeProperties) {
        this.challengeProperties = challengeProperties;
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(challengeProperties.getMaxSessions());
    }
}
