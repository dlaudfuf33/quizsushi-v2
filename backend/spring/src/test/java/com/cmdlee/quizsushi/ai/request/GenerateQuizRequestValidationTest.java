package com.cmdlee.quizsushi.ai.request;

import com.cmdlee.quizsushi.quiz.dto.request.GenerateQuizRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateQuizRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("문제 수가 0일 경우 유효성 검사 실패")
    void countIsTooLow_shouldFailValidation() {
        GenerateQuizRequest request = GenerateQuizRequest.builder()
                .topic("Java")
                .difficulty("easy")
                .count(0)
                .build();

        Set<ConstraintViolation<GenerateQuizRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).contains("최소 1개의 문제를 생성해야 합니다");
    }

    @Test
    @DisplayName("문제 수가 4일 경우 유효성 검사 실패")
    void countIsTooHigh_shouldFailValidation() {
        GenerateQuizRequest request = GenerateQuizRequest.builder()
                .topic("Java")
                .difficulty("medium")
                .count(4)
                .build();

        Set<ConstraintViolation<GenerateQuizRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).contains("최대 3개의 문제만 생성할 수 있습니다");
    }

    @Test
    @DisplayName("문제 수가 1~3 사이면 유효성 검사 통과")
    void countInValidRange_shouldPassValidation() {
        for (int i = 1; i <= 3; i++) {
            GenerateQuizRequest request = GenerateQuizRequest.builder()
                    .topic("Java")
                    .difficulty("medium")
                    .count(i)
                    .build();

            Set<ConstraintViolation<GenerateQuizRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }
    }
}