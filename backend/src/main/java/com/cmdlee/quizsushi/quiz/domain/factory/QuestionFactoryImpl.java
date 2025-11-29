package com.cmdlee.quizsushi.quiz.domain.factory;

import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.quiz.domain.model.Quiz;
import com.cmdlee.quizsushi.quiz.domain.model.question.*;
import com.cmdlee.quizsushi.quiz.domain.model.question.sub.MatchingPair;
import com.cmdlee.quizsushi.quiz.domain.model.question.sub.MultipleOption;
import com.cmdlee.quizsushi.quiz.domain.model.question.sub.OrderingOption;
import com.cmdlee.quizsushi.quiz.dto.creation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuestionFactoryImpl implements QuestionFactory {

    @Override
    public BaseQuestion create(QuestionCreationData data, Quiz quiz) {
        if (data instanceof MultipleCreationData multiple) {
            return toMultiple(multiple, quiz);
        } else if (data instanceof ShortsCreationData shorts) {
            return toShorts(shorts, quiz);
        } else if (data instanceof OrderingCreationData ordering) {
            return toOrdering(ordering, quiz);
        } else if (data instanceof MatchingCreationData matching) {
            return toMatching(matching, quiz);
        } else {
            throw new GlobalException(ErrorCode.INVALID_QUESTION_TYPE);
        }
    }

    private QuestionMultiple toMultiple(MultipleCreationData data, Quiz quiz) {
        List<MultipleOption> options = data.getMultipleOptions().stream()
                .map(opt -> new MultipleOption(opt.getText(), opt.isCorrect()))
                .toList();

        QuestionMultiple question = new QuestionMultiple(
                data.getNo(),
                data.getSubject(),
                data.getQuestionText(),
                data.getExplanation(),
                quiz,
                options
        );

        options.forEach(o -> o.setQuestion(question));

        return question;
    }

    private QuestionShorts toShorts(ShortsCreationData data, Quiz quiz) {
        return new QuestionShorts(
                data.getNo(),
                data.getQuestionText(),
                data.getSubject(),
                data.getExplanation(),
                quiz,
                data.getShortsAnswerList()
        );
    }

    private QuestionOrdering toOrdering(OrderingCreationData data, Quiz quiz) {
        QuestionOrdering question = new QuestionOrdering(
                data.getNo(),
                data.getQuestionText(),
                data.getSubject(),
                data.getExplanation(),
                quiz
        );

        List<OrderingOption> options = data.getOrderingOptionDataList().stream()
                .map(opt -> new OrderingOption(question, opt.getText(), opt.getOrdering()))
                .toList();
        question.setOrderingOptionList(options);
        return question;
    }

    private QuestionMatching toMatching(MatchingCreationData data, Quiz quiz) {
        QuestionMatching question = new QuestionMatching(
                data.getNo(),
                data.getSubject(),
                data.getQuestionText(),
                data.getExplanation(),
                quiz
        );

        List<MatchingPair> pairs = data.getMatchingPairs().stream()
                .map(pair -> new MatchingPair(question, pair.getLeftText(), pair.getRightText()))
                .toList();
        question.setPairs(pairs);
        return question;
    }
}
