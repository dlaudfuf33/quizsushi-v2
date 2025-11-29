package com.cmdlee.quizsushi.quiz.dto.response;

import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.quiz.domain.model.Question;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateQuizResponse {
    private Integer no;
    private String type;
    private String subject;
    private String question;
    private List<String> options;
    private List<Integer> correctAnswer;
    private String correctAnswerText;
    private String explanation;

    public static GenerateQuizResponse from(Question question) {
        ObjectMapper mapper = new ObjectMapper();
        List<String> options = List.of();
        List<Integer> correctAnswer = List.of();

        try {
            if (question.getOptions() != null) {
                options = mapper.readValue(question.getOptions(), new TypeReference<>() {});
            }
            if (question.getCorrectIndexes() != null) {
                correctAnswer = mapper.readValue(question.getCorrectIndexes(), new TypeReference<>() {});
            }
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
        return GenerateQuizResponse.builder()
                .no(question.getNo())
                .type(question.getType().toString())
                .subject(question.getSubject())
                .question(question.getQuestionText())
                .options(options)
                .correctAnswer(correctAnswer)
                .correctAnswerText(question.getCorrectAnswerText())
                .explanation(question.getExplanation())
                .build();
    }
    @Override
    public String toString() {
        return "\n📘 GenerateQuizResponse {" +
                "\n  📌 no = " + no +
                "\n  🧩 type = '" + type + '\'' +
                "\n  📚 subject = '" + subject + '\'' +
                "\n  ❓ question = '" + question + '\'' +
                "\n  🔘 options = " + options +
                "\n  ✅ correctAnswer = " + correctAnswer +
                "\n  ✍️ correctAnswerText = '" + correctAnswerText + '\'' +
                "\n  💡 explanation = '" + explanation + '\'' +
                "\n}";
    }
}

