package com.cmdlee.quizsushi.quiz.challenge.dto.response;

import com.cmdlee.quizsushi.quiz.challenge.model.PlayerStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerStateResponse {
    private String nickname;
    private String avatar;
    private int hp;
    private int maxHp;
    private long score;
    private int combo;

    private boolean answered;
    private String submittedAnswer;

    public static PlayerStateResponse of(PlayerStatus playerStatus) {
        return PlayerStateResponse.builder()
                .nickname(playerStatus.getNickname())
                .avatar(playerStatus.getAvatar())
                .hp(playerStatus.getHp())
                .maxHp(playerStatus.getMaxHp())
                .score(playerStatus.getScore())
                .combo(playerStatus.getCombo())
                .answered(playerStatus.isAnswered())
                .submittedAnswer(playerStatus.getSubmittedAnswer())
                .build();
    }
}
