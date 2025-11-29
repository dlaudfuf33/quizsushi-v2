package com.cmdlee.quizsushi.quiz.domain.factory;

import com.cmdlee.quizsushi.quiz.domain.model.Quiz;
import com.cmdlee.quizsushi.quiz.dto.QuizCreationData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultQuizFactory implements QuizFactory{

    @Override
    public Quiz create(QuizCreationData data) {
        return Quiz.builder()
                .author(data.getAuthor())
                .title(data.getTitle())
                .useSubject(data.isUseSubject())
                .questionCount(data.getQuestionCount())
                .mediaKey(data.getMediaKey())
                .description(data.getDescription())
                .category(data.getCategory())
                .build();

    }
}
