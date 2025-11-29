package com.cmdlee.quizsushi.quiz.service;

import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import com.cmdlee.quizsushi.quiz.domain.model.Category;
import com.cmdlee.quizsushi.quiz.dto.QuizCreationData;
import com.cmdlee.quizsushi.quiz.dto.request.CreateQuizRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuizPreprocessingService {

    public QuizCreationData process(CreateQuizRequest request, QuizsushiMember member, Category category, String mediaKey) {
        return QuizCreationData.builder()
                .author(member)
                .title(request.getTitle())
                .category(category)
                .description(request.getDescription())
                .useSubject(request.isUseSubject())
                .mediaKey(mediaKey)
                .questionCount(request.getQuestions().size())
                .build();

    }
}
