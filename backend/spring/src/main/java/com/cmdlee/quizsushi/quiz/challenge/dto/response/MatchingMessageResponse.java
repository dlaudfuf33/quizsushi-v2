package com.cmdlee.quizsushi.quiz.challenge.dto.response;

import com.cmdlee.quizsushi.quiz.challenge.model.enums.MatchStatusType;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatchingMessageResponse {
    private MatchStatusType status;
    private String sessionId;

    @Nullable
    String noticeMessage;

    public static MatchingMessageResponse from(MatchStatusType status) {
        return MatchingMessageResponse.builder()
                .status(status)
                .build();
    }

    public static MatchingMessageResponse of(MatchStatusType status, String sessionId) {
        return MatchingMessageResponse.builder()
                .status(status)
                .sessionId(sessionId)
                .build();
    }

    public static MatchingMessageResponse of(MatchStatusType status, String sessionId, String noticeMessage) {
        return MatchingMessageResponse.builder()
                .status(status)
                .sessionId(sessionId)
                .noticeMessage(noticeMessage)
                .build();
    }

    public static MatchingMessageResponse disabled(String noticeMessage) {
        return MatchingMessageResponse.builder()
                .status(MatchStatusType.DISABLED)
                .noticeMessage(noticeMessage)
                .build();
    }
}
