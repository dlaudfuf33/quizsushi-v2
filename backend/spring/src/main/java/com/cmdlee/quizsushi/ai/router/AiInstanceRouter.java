package com.cmdlee.quizsushi.ai.router;

import com.cmdlee.quizsushi.global.config.ai.AiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class AiInstanceRouter {

    private final AiProperties aiProperties;
    private final AtomicInteger index = new AtomicInteger(0);

    public String nextUrl() {
        List<String> urls = aiProperties.getUrls();
        int i = index.getAndUpdate(n -> (n + 1) % urls.size());
        return urls.get(i);
    }
}
