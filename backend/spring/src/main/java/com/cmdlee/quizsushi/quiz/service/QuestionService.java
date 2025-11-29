package com.cmdlee.quizsushi.quiz.service;

import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.quiz.domain.factory.QuestionFactory;
import com.cmdlee.quizsushi.quiz.domain.model.Quiz;
import com.cmdlee.quizsushi.quiz.domain.model.question.*;
import com.cmdlee.quizsushi.quiz.dto.creation.QuestionCreationData;
import com.cmdlee.quizsushi.quiz.dto.request.question.create.CreateQuestionRequest;
import com.cmdlee.quizsushi.quiz.dto.request.question.update.UpdateQuestionRequest;
import com.cmdlee.quizsushi.quiz.repository.question.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionPreprocessingService questionPreprocessingService;
    private final QuestionFactory questionFactory;

    private final List<BaseQuestionRepository<? extends BaseQuestion>> repositories;

    public List<BaseQuestion> findAllQuestionsByQuizId(Long quizId) {
        return repositories.stream()
                .flatMap(repo -> repo.findByQuizId(quizId).stream())
                .map(q -> (BaseQuestion) q)
                .toList();
    }

    // 문제 생성
    public List<BaseQuestion> createQuestions(List<CreateQuestionRequest> requests, Quiz quiz) {
        List<QuestionCreationData> dataList = questionPreprocessingService.processForCreate(
                requests, quiz.isUseSubject(), quiz.getMediaKey()
        );

        List<BaseQuestion> questions = dataList.stream()
                .map(data -> questionFactory.create(data, quiz))
                .toList();

        persistNewQuestions(questions);
        return questions;
    }


    // 문제 업데이트
    public List<BaseQuestion> updateQuestions(List<UpdateQuestionRequest> requests, Quiz quiz) {
        // 기존 문제들 조회 및 Map 구성
        Map<Long, BaseQuestion> existingMap = findAllQuestionsByQuizId(quiz.getId()).stream()
                .collect(Collectors.toMap(BaseQuestion::getId, q -> q));

        List<BaseQuestion> result = new ArrayList<>();
        List<BaseQuestion> toDeleteList = new ArrayList<>();

        handleDeletions(requests, existingMap, toDeleteList);
        handleCreations(requests, quiz, result);
        handleModifications(requests, existingMap, result);

        deleteRemovedQuestions(toDeleteList);
        persistNewQuestions(result.stream().filter(q -> q.getId() == null).toList());

        return result;
    }

    public void deleteRemovedQuestions(Collection<BaseQuestion> toDelete) {
        Map<Class<?>, List<BaseQuestion>> grouped = toDelete.stream()
                .collect(Collectors.groupingBy(BaseQuestion::getClass));

        for (var entry : grouped.entrySet()) {
            Class<?> clazz = entry.getKey();
            List<BaseQuestion> group = entry.getValue();
            findMatchingRepo(clazz).deleteAll(group);
        }
    }

    private void handleDeletions(List<UpdateQuestionRequest> requests,
                                 Map<Long, BaseQuestion> existingMap,
                                 List<BaseQuestion> toDeleteList) {
        for (UpdateQuestionRequest req : requests) {
            if (req.isDelete()) {
                BaseQuestion toDelete = existingMap.remove(req.getId());
                if (toDelete == null) throw new GlobalException(ErrorCode.ENTITY_NOT_FOUND);
                toDeleteList.add(toDelete);
            }
        }
    }

    private void handleCreations(List<UpdateQuestionRequest> requests,
                                 Quiz quiz,
                                 List<BaseQuestion> result) {
        for (UpdateQuestionRequest req : requests) {
            if (req.getId() == null && !req.isDelete()) {
                QuestionCreationData newData = questionPreprocessingService
                        .processForUpdate(List.of(req), quiz.isUseSubject(), quiz.getMediaKey())
                        .get(0);
                result.add(questionFactory.create(newData, quiz));
            }
        }
    }

    private void handleModifications(List<UpdateQuestionRequest> requests,
                                     Map<Long, BaseQuestion> existingMap,
                                     List<BaseQuestion> result) {
        for (UpdateQuestionRequest req : requests) {
            if (req.getId() != null && !req.isDelete()) {
                BaseQuestion existing = existingMap.remove(req.getId());
                if (existing == null) throw new GlobalException(ErrorCode.ENTITY_NOT_FOUND);

                if (isModified(existing, req)) {
                    updateQuestion(existing, req);
                }
                result.add(existing);
            }
        }
    }


    // 문제 저장
    public void persistNewQuestions(List<BaseQuestion> questions) {
        Map<Class<?>, List<BaseQuestion>> grouped = questions.stream()
                .collect(Collectors.groupingBy(BaseQuestion::getClass));

        for (var entry : grouped.entrySet()) {
            Class<?> clazz = entry.getKey();
            List<BaseQuestion> group = entry.getValue();
            findMatchingRepo(clazz).saveAll(group);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends BaseQuestion> BaseQuestionRepository<T> findMatchingRepo(Class<?> clazz) {
        return (BaseQuestionRepository<T>) repositories.stream()
                .filter(r -> r.getDomainClass().equals(clazz))
                .findFirst()
                .orElseThrow(() -> new GlobalException(ErrorCode.INVALID_QUESTION_TYPE));
    }

    public boolean isModified(BaseQuestion existing, UpdateQuestionRequest req) {
        return !existing.equalsContent(req);
    }

    public void updateQuestion(BaseQuestion existing, UpdateQuestionRequest req) {
        existing.updateFrom(req);
    }


}
