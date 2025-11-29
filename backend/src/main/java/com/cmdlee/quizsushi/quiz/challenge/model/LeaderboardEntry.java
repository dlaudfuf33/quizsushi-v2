package com.cmdlee.quizsushi.quiz.challenge.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardEntry implements Serializable {
    private String memberId;
    private String nickname;
    private String avatar;
    private long bestScore;

    public LeaderboardEntry(String memberId, String nickname, String avatar) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.avatar = avatar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaderboardEntry that = (LeaderboardEntry) o;
        return Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId);
    }
}