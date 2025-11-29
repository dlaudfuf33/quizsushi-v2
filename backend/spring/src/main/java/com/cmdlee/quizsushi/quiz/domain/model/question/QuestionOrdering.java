package com.cmdlee.quizsushi.quiz.domain.model.question;

import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.quiz.domain.model.Quiz;
import com.cmdlee.quizsushi.quiz.domain.model.enums.QuestionType;
import com.cmdlee.quizsushi.quiz.domain.model.question.sub.OrderingOption;
import com.cmdlee.quizsushi.quiz.dto.request.question.update.UpdateOrderingOptionData;
import com.cmdlee.quizsushi.quiz.dto.request.question.update.UpdateOrderingQuestionRequest;
import com.cmdlee.quizsushi.quiz.dto.request.question.update.UpdateQuestionRequest;
import com.cmdlee.quizsushi.quiz.dto.response.question.OrderingQuestionResponse;
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
@Table(name = "question_ordering")
public class QuestionOrdering extends BaseQuestion {


    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderingOption> orderingOptionList = new ArrayList<>();

    public QuestionOrdering(Integer no, String subject, String questionText,
                            String explanation, Quiz quiz) {
        super(no, QuestionType.ORDERING, subject, questionText, explanation, quiz);
    }

    public void setOrderingOptionList(List<OrderingOption> orderingOptionList) {
        this.orderingOptionList = orderingOptionList;
    }

    @Override
    public boolean equalsContent(UpdateQuestionRequest req) {
        if (!(req instanceof UpdateOrderingQuestionRequest r)) return false;
        return this.getNo() == r.getNo()
                && Objects.equals(this.getSubject(), r.getSubject())
                && Objects.equals(this.getQuestionText(), r.getQuestionText())
                && Objects.equals(this.getExplanation(), r.getExplanation())
                && orderingOptionListEquals(this.orderingOptionList, r.getOrderingOptionDataList());
    }

    private boolean orderingOptionListEquals(List<OrderingOption> oldOrderingOptionList, List<UpdateOrderingOptionData> newOrderingOptionDataList) {
        if (oldOrderingOptionList.size() != newOrderingOptionDataList.size()) return false;

        for (int i = 0; i < oldOrderingOptionList.size(); i++) {
            OrderingOption oldOrderingOption = oldOrderingOptionList.get(i);
            UpdateOrderingOptionData newOrderingOptionData = newOrderingOptionDataList.get(i);
            if (!Objects.equals(oldOrderingOption.getId(), newOrderingOptionData.getId()) ||
                    !oldOrderingOption.getText().equals(newOrderingOptionData.getText()) ||
                    oldOrderingOption.getOrdering() != newOrderingOptionData.getOrdering())
                return false;
        }
        return true;
    }

    @Override
    public void updateFrom(UpdateQuestionRequest req) {
        if (!(req instanceof UpdateOrderingQuestionRequest r)) {
            throw new GlobalException(ErrorCode.INVALID_QUESTION_TYPE);
        }

        this.no = r.getNo();
        this.subject = r.getSubject();
        this.questionText = r.getQuestionText();
        this.explanation = r.getExplanation();

        this.orderingOptionList.clear();
        for (UpdateOrderingOptionData opt : r.getOrderingOptionDataList()) {
            this.orderingOptionList.add(OrderingOption.of(this, opt));
        }
    }

    @Override
    public QuestionResponse toResponse() {
        List<String> suffleList = new ArrayList<>(orderingOptionList.stream()
                .map(OrderingOption::getText)
                .toList());
        Collections.shuffle(suffleList);
        return OrderingQuestionResponse.builder()
                .id(this.getId())
                .no(this.getNo())
                .type(this.getType())
                .subject(this.getSubject())
                .questionText(this.getQuestionText())
                .orderingOptionList(suffleList)
                .build();
    }

}

