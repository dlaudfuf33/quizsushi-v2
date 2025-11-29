package com.cmdlee.quizsushi.quiz.challenge.model;

import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerStatus implements Serializable {
    private String memberId;
    private String nickname;
    private String avatar;

    private int hp;
    private int maxHp;
    private long score;
    private int combo;

    private boolean answered = false;
    private String submittedAnswer = "";

    private boolean connected = true;
    private Instant disconnectedAt;

    private Instant lastActivityTime;


    public PlayerStatus(QuizsushiMember member, int baseHP) {
        this.memberId = String.valueOf(member.getId());
        this.nickname = member.getNickname();
        this.avatar = member.getProfileImage();
        this.hp = baseHP;
        this.maxHp = baseHP;
        this.score = 0;
        this.combo = 0;
        this.lastActivityTime = Instant.now();
    }

    public void resetAnswerState() {
        this.answered = false;
        this.submittedAnswer = "";
    }

    public void submitAnswer(String submittedAnswer) {
        this.answered = true;
        this.submittedAnswer = submittedAnswer;
    }

    public int increaseScoreWithCombo(int baseScore) {
        double multiplier = getComboMultiplier();
        int earned = (int) Math.round(baseScore * multiplier);
        this.score += earned;
        combo++;
        return earned;
    }

    public void wrongAnswer() {
        combo = 0;
        hp--;
    }

    private double getComboMultiplier() {
        double[] multipliers = {1.0, 1.25, 1.5, 2.0, 2.5, 3.0};
        int index = Math.min(combo, multipliers.length - 1);
        return multipliers[index];
    }

    @JsonIgnore
    public boolean isDead() {
        return hp <= 0;
    }

    public void updateActivityTime() {
        lastActivityTime = Instant.now();
    }

    public void forceEliminate() {
        this.hp = 0;
        this.connected = false;
        this.disconnectedAt = Instant.now();
    }
}