package com.cmdlee.quizsushi.quiz.challenge.dto.response;

import com.cmdlee.quizsushi.quiz.challenge.dto.request.ChatMessageRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse implements Serializable {
    private Instant chatAt;
    private String nickname;
    private String content;

    public ChatMessageResponse(String nickname, String content, Instant chatAt) {
        this.nickname = nickname;
        this.content = content;
        this.chatAt = chatAt;
    }

    public static ChatMessageResponse from(ChatMessageRequest chatMessageRequest) {
        return new ChatMessageResponse(chatMessageRequest.getNickname(), chatMessageRequest.getContent(), chatMessageRequest.getChatAt());
    }

    @Override
    public String toString() {
        return "[" + chatAt + "] " + nickname + ": \n" + content;
    }
}