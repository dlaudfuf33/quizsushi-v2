package com.cmdlee.quizsushi.global.config.ai;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiStartupChecker {

    private final AiProperties aiProperties;

    @PostConstruct
    public void logAiProperties() {
        log.info("AI_URLS: {}\nAI_STREAM: {}", aiProperties.getUrls(), aiProperties.isStream());
    }
}
