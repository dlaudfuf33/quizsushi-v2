package com.cmdlee.quizsushi.report.handler;

import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.quiz.repository.QuizRepository;
import com.cmdlee.quizsushi.report.model.enums.ReportTargetType;
import org.springframework.stereotype.Component;

@Component
public class QuizReportHandler implements ReportTargetHandler {

    private final QuizRepository quizRepository;

    public QuizReportHandler(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    @Override
    public ReportTargetType getTargetType() {
        return ReportTargetType.QUIZ;
    }

    @Override
    public void validateTargetExists(Long targetId) {
        if (!quizRepository.existsById(targetId)) {
            throw new GlobalException(ErrorCode.ENTITY_NOT_FOUND);
        }
    }
}
