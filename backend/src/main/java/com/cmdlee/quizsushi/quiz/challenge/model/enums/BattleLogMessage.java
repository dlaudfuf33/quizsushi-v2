package com.cmdlee.quizsushi.quiz.challenge.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BattleLogMessage {
    START("게임 시작!"),
    FINISH_GENERATING("문제 생성 완료!"),
    CORRECT("정답!"),
    WRONG("오답!"),
    TIMEOUT("시간 초과!"),
    END("게임 종료!");

    private final String message;
}
