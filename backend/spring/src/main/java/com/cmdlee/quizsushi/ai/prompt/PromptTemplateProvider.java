package com.cmdlee.quizsushi.ai.prompt;

public interface PromptTemplateProvider {
    String getTemplate(String taskName);
}
