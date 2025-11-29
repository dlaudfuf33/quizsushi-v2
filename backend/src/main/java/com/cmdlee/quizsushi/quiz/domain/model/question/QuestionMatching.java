package com.cmdlee.quizsushi.quiz.domain.model.question;

import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.quiz.domain.model.Quiz;
import com.cmdlee.quizsushi.quiz.domain.model.enums.QuestionType;
import com.cmdlee.quizsushi.quiz.domain.model.question.sub.MatchingPair;
import com.cmdlee.quizsushi.quiz.dto.request.question.update.UpdateMatchingPairData;
import com.cmdlee.quizsushi.quiz.dto.request.question.update.UpdateMatchingQuestionRequest;
import com.cmdlee.quizsushi.quiz.dto.request.question.update.UpdateQuestionRequest;
import com.cmdlee.quizsushi.quiz.dto.response.question.MatchingQuestionResponse;
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
@Table(name = "question_matching")
public class QuestionMatching extends BaseQuestion {

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchingPair> matchingPairList = new ArrayList<>();

    public QuestionMatching(Integer no, String subject, String questionText,
                            String explanation, Quiz quiz) {
        super(no, QuestionType.MATCHING, subject, questionText, explanation, quiz);
    }

    public void setPairs(List<MatchingPair> pairs) {
        this.matchingPairList = pairs;
    }


    @Override
    public boolean equalsContent(UpdateQuestionRequest req) {
        if (!(req instanceof UpdateMatchingQuestionRequest r)) return false;
        return this.getNo() == r.getNo()
                && Objects.equals(this.getSubject(), r.getSubject())
                && Objects.equals(this.getQuestionText(), r.getQuestionText())
                && Objects.equals(this.getExplanation(), r.getExplanation())
                && matchingPairEquals(this.matchingPairList, r.getMatchingPairDataList());
    }

    private boolean matchingPairEquals(List<MatchingPair> oldMatchingPairList, List<UpdateMatchingPairData> newMatchingPairDataList) {
        if (oldMatchingPairList.size() != newMatchingPairDataList.size()) return false;

        for (int i = 0; i < oldMatchingPairList.size(); i++) {
            MatchingPair oldPair = oldMatchingPairList.get(i);
            UpdateMatchingPairData newPair = newMatchingPairDataList.get(i);
            if (!Objects.equals(oldPair.getId(), newPair.getId()) ||
                    !oldPair.getLeftText().equals(newPair.getLeftText()) ||
                    !oldPair.getRightText().equals(newPair.getRightText()))
                return false;
        }
        return true;
    }

    @Override
    public void updateFrom(UpdateQuestionRequest req) {
        if (!(req instanceof UpdateMatchingQuestionRequest r)) {
            throw new GlobalException(ErrorCode.INVALID_QUESTION_TYPE);
        }

        this.no = r.getNo();
        this.subject = r.getSubject();
        this.questionText = r.getQuestionText();
        this.explanation = r.getExplanation();

        this.matchingPairList.clear();
        for (UpdateMatchingPairData newPair : r.getMatchingPairDataList()) {
            this.matchingPairList.add(MatchingPair.of(this, newPair));
        }
    }

    @Override
    public QuestionResponse toResponse() {
        List<String> suffleLeftList = new ArrayList<>(matchingPairList.stream().map(MatchingPair::getLeftText).toList());
        List<String> suffleRightList = new ArrayList<>(matchingPairList.stream().map(MatchingPair::getRightText).toList());
        Collections.shuffle(suffleLeftList);
        Collections.shuffle(suffleRightList);

        return MatchingQuestionResponse.builder()
                .id(this.getId())
                .no(this.getNo())
                .type(this.getType())
                .subject(this.getSubject())
                .leftList(suffleLeftList)
                .rightList(suffleRightList)
                .build();
    }
}
