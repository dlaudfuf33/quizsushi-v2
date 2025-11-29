package com.cmdlee.quizsushi.global.config.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.stereotype.Component; // Component 어노테이션 추가

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    private List<String> urls;
    private boolean stream;
}
