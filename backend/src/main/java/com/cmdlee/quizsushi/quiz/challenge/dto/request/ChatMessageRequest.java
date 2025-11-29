package com.cmdlee.quizsushi.quiz.challenge.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest implements Serializable {
    private String sessionId;
    private String senderId;
    private Instant chatAt = Instant.now();
    private String nickname;
    private String content;

    public ChatMessageRequest(String nickname, String content) {
        this.nickname = nickname;
        this.content = content;
    }

    @Override
    public String toString() {
        return "[" + sessionId + ":: " + chatAt + "] " + senderId + ": " + nickname + ": \n" + content;
    }
}