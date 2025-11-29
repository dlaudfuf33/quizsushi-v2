package com.cmdlee.quizsushi.quiz.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParsedProblem {
    private String type;
    private String question;
    private List<String> options;
    private String correctAnswer;
    private String correctAnswerText;
    private String explanation;

    public static ParsedProblem fromBlock(String block) {
        return ParsedProblem.builder()
                .type(Optional.ofNullable(extract(block, "\\{type}:\\s*(.+?)¶")).orElse(""))
                .question(Optional.ofNullable(buildQuestion(block)).orElse(""))
                .options(Optional.ofNullable(extractOptions(block)).orElse(List.of()))
                .correctAnswer(Optional.ofNullable(extract(block, "\\{correctAnswer}:\\s*(\\d+)¶")).orElse(""))
                .correctAnswerText(Optional.ofNullable(extract(block, "\\{correctAnswerText}:\\s*(.+?)¶")).orElse(""))
                .explanation(Optional.ofNullable(extract(block, "\\{explanation}:\\s*(.+)")).orElse(""))
                .build();
    }

    private static String extract(String text, String pattern) {
        Matcher matcher = Pattern.compile(pattern).matcher(text);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private static String buildQuestion(String block) {
        String question = extract(block, "\\{question}:\\s*(.+?)¶");
        Matcher codeMatcher = Pattern.compile("```(.+?)```", Pattern.DOTALL).matcher(block);
        if (codeMatcher.find()) {
            question += "\n```\n" + codeMatcher.group(1).trim() + "\n```";
        }
        return question;
    }

    private static List<String> extractOptions(String block) {
        String raw = extract(block, "\\{options}:\\[(.*?)\\]¶");
        if (raw == null) return List.of();
        return Arrays.stream(raw.split(","))
                .map(s -> s.replaceAll("(^\")|(\"$)", "").trim())
                .toList();
    }
}