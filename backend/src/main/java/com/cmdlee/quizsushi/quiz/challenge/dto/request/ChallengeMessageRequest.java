package com.cmdlee.quizsushi.quiz.challenge.dto.request;
import lombok.Data;

@Data
public class ChallengeMessageRequest {
    private String sessionId;
    private String senderId;
    private String content;
}