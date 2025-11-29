package com.cmdlee.quizsushi.quiz.domain.model.question;

import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.quiz.domain.model.Quiz;
import com.cmdlee.quizsushi.quiz.domain.model.enums.QuestionType;
import com.cmdlee.quizsushi.quiz.domain.model.question.sub.MultipleOption;
import com.cmdlee.quizsushi.quiz.dto.request.question.update.UpdateMultipleOptionData;
import com.cmdlee.quizsushi.quiz.dto.request.question.update.UpdateMultipleQuestionRequest;
import com.cmdlee.quizsushi.quiz.dto.request.question.update.UpdateQuestionRequest;
import com.cmdlee.quizsushi.quiz.dto.response.question.MultipleOptionResponse;
import com.cmdlee.quizsushi.quiz.dto.response.question.MultipleQuestionResponse;
import com.cmdlee.quizsushi.quiz.dto.response.question.QuestionResponse;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@NoArgsConstructor
@Table(name = "question_multiple")
public class QuestionMultiple extends BaseQuestion {

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MultipleOption> optionList = new ArrayList<>();

    public QuestionMultiple(Integer no, String subject, String questionText, String explanation, Quiz quiz,
                            List<MultipleOption> options) {
        super(no, QuestionType.MULTIPLE, subject, questionText, explanation, quiz);
        this.optionList = options;
    }

    @Override
    public boolean equalsContent(UpdateQuestionRequest req) {
        if (!(req instanceof UpdateMultipleQuestionRequest r)) return false;

        return this.getNo() == r.getNo()
                && Objects.equals(this.getSubject(), r.getSubject())
                && Objects.equals(this.getQuestionText(), r.getQuestionText())
                && Objects.equals(this.getExplanation(), r.getExplanation())
                && optionsEquals(this.optionList, r.getOptionDataList());
    }

    private boolean optionsEquals(List<MultipleOption> oldOptionDataList, List<UpdateMultipleOptionData> newOptionDataList) {
        if (oldOptionDataList.size() != newOptionDataList.size()) return false;

        for (int i = 0; i < oldOptionDataList.size(); i++) {
            MultipleOption oldOpt = oldOptionDataList.get(i);
            UpdateMultipleOptionData newOpt = newOptionDataList.get(i);
            if (!Objects.equals(oldOpt.getText(), newOpt.getText()) ||
                    oldOpt.isCorrect() != newOpt.isCorrect()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void updateFrom(UpdateQuestionRequest req) {
        if (!(req instanceof UpdateMultipleQuestionRequest r)) {
            throw new GlobalException(ErrorCode.INVALID_QUESTION_TYPE);
        }

        this.no = r.getNo();
        this.subject = r.getSubject();
        this.questionText = r.getQuestionText();
        this.explanation = r.getExplanation();

        this.optionList.clear();
        for (UpdateMultipleOptionData option : r.getOptionDataList()) {
            this.optionList.add(MultipleOption.of(this, option));
        }
    }

    @Override
    public QuestionResponse toResponse() {
        List<MultipleOptionResponse> suffleList = new ArrayList<>(this.optionList.stream()
                .map(opt -> new MultipleOptionResponse(opt.getId(),opt.getText()))
                .toList());
        Collections.shuffle(suffleList);

        return MultipleQuestionResponse.builder()
                .id(this.getId())
                .no(this.getNo())
                .type(this.getType())
                .subject(this.getSubject())
                .questionText(this.getQuestionText())
                .optionList(suffleList)
                .build();
    }

}
