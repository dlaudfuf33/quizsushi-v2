package com.cmdlee.quizsushi.global.config.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "challenge")
@Getter
@Setter
public class ChallengeProperties {
    private int maxSessions;
}
