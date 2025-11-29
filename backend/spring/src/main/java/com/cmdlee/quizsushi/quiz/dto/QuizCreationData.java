package com.cmdlee.quizsushi.quiz.dto;

import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import com.cmdlee.quizsushi.quiz.domain.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class QuizCreationData {
    QuizsushiMember author;
    String title;
    Category category;
    String description;
    boolean useSubject;
    String mediaKey;
    int questionCount;
}