package com.cmdlee.quizsushi.quiz.challenge.model.dto;

import lombok.Getter;

@Getter
public class PlayerAnswer {
    private final String memberId;
    private final String nickname;
    private final String avatar;
    private final String submittedAnswer;

    public PlayerAnswer(String memberId, String nickname, String avatar, String submittedAnswer) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.avatar = avatar;
        this.submittedAnswer = submittedAnswer;
    }

    @Override
    public String toString() {
        return "PlayerAnswer{" +
                "memberId='" + memberId + '\'' +
                ", nickname='" + nickname + '\'' +
                ", submittedAnswer='" + submittedAnswer + '\'' +
                '}';
    }
}
