package com.cmdlee.quizsushi.ai.prompt;

public interface PromptBuilder<T> {
    String build(String taskName, T context);
}
