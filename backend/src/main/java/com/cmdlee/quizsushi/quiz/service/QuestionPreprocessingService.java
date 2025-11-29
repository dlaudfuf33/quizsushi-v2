package com.cmdlee.quizsushi.quiz.service;

import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.minio.service.MinioService;
import com.cmdlee.quizsushi.quiz.dto.creation.*;
import com.cmdlee.quizsushi.quiz.dto.request.question.create.*;
import com.cmdlee.quizsushi.quiz.dto.request.question.update.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuestionPreprocessingService {
    private final MinioService minioService;

    public QuestionCreationData processing(CreateQuestionRequest request, boolean useSubject, String mediaKey) {
        if (request instanceof CreateMultipleQuestionRequest multiple) {
            return MultipleCreationData.of(
                    multiple.getNo(),
                    useSubject ? multiple.getSubject() : "",
                    minioService.rewriteTempMediaLinks(multiple.getQuestionText(), mediaKey),
                    multiple.getOptionDataList().stream()
                            .map(opt -> new MultipleOptionData(
                                    minioService.rewriteTempMediaLinks(opt.getText(), mediaKey),
                                    opt.isCorrect()
                            ))
                            .toList(),
                    minioService.rewriteTempMediaLinks(multiple.getExplanation(), mediaKey)
            );
        } else if (request instanceof CreateShortsQuestionRequest shorts) {
            return ShortsCreationData.of(
                    shorts.getNo(),
                    useSubject ? shorts.getSubject() : "",
                    minioService.rewriteTempMediaLinks(shorts.getQuestionText(), mediaKey),
                    shorts.getShortsAnswerList().stream().toList(),
                    minioService.rewriteTempMediaLinks(shorts.getExplanation(), mediaKey)
            );
        } else if (request instanceof CreateOrderingQuestionRequest ordering) {
            return OrderingCreationData.of(
                    ordering.getNo(),
                    useSubject ? ordering.getSubject() : "",
                    minioService.rewriteTempMediaLinks(ordering.getQuestionText(), mediaKey),
                    ordering.getOrderingOptionDataList().stream()
                            .map(opt -> new OrderingOptionData(
                                    minioService.rewriteTempMediaLinks(opt.getText(), mediaKey),
                                    opt.getOrdering()
                            ))
                            .toList(),
                    minioService.rewriteTempMediaLinks(ordering.getExplanation(), mediaKey)
            );
        } else if (request instanceof CreateMatchingQuestionRequest matching) {
            return MatchingCreationData.of(
                    matching.getNo(),
                    useSubject ? matching.getSubject() : "",
                    minioService.rewriteTempMediaLinks(matching.getQuestionText(), mediaKey),
                    matching.getMatchingPairDataList().stream()
                            .map(pair -> new MatchingPairData(
                                    minioService.rewriteTempMediaLinks(pair.getLeftText(), mediaKey),
                                    minioService.rewriteTempMediaLinks(pair.getRightText() , mediaKey)
                            ))
                            .toList(),
                    minioService.rewriteTempMediaLinks(matching.getExplanation(), mediaKey)
            );
        } else {
            throw new GlobalException(ErrorCode.INVALID_QUESTION_TYPE);
        }
    }

    public QuestionCreationData processing(UpdateQuestionRequest request, boolean useSubject, String mediaKey) {
        if (request instanceof UpdateMultipleQuestionRequest multiple) {
            return MultipleCreationData.of(
                    multiple.getNo(),
                    useSubject ? multiple.getSubject() : "",
                    minioService.rewriteTempMediaLinks(multiple.getQuestionText(), mediaKey),
                    multiple.getOptionDataList().stream()
                            .map(opt -> new MultipleOptionData(
                                    minioService.rewriteTempMediaLinks(opt.getText(), mediaKey),
                                    opt.isCorrect()
                            ))
                            .toList(),
                    minioService.rewriteTempMediaLinks(multiple.getExplanation(), mediaKey)
            );
        } else if (request instanceof UpdateShortsQuestionRequest shorts) {
            return ShortsCreationData.of(
                    shorts.getNo(),
                    useSubject ? shorts.getSubject() : "",
                    minioService.rewriteTempMediaLinks(shorts.getQuestionText(), mediaKey),
                    shorts.getCorrectAnswerList().stream().toList(),
                    minioService.rewriteTempMediaLinks(shorts.getExplanation(), mediaKey)
            );
        } else if (request instanceof UpdateOrderingQuestionRequest ordering) {
            return OrderingCreationData.of(
                    ordering.getNo(),
                    useSubject ? ordering.getSubject() : "",
                    minioService.rewriteTempMediaLinks(ordering.getQuestionText(), mediaKey),
                    ordering.getOrderingOptionDataList().stream()
                            .map(opt -> new OrderingOptionData(
                                    minioService.rewriteTempMediaLinks(opt.getText(), mediaKey),
                                    opt.getOrdering()
                            ))
                            .toList(),
                    minioService.rewriteTempMediaLinks(ordering.getExplanation(), mediaKey)
            );
        } else if (request instanceof UpdateMatchingQuestionRequest matching) {
            return MatchingCreationData.of(
                    matching.getNo(),
                    useSubject ? matching.getSubject() : "",
                    minioService.rewriteTempMediaLinks(matching.getQuestionText(), mediaKey),
                    matching.getMatchingPairDataList().stream()
                            .map(pair -> new MatchingPairData(
                                    minioService.rewriteTempMediaLinks(pair.getLeftText(), mediaKey),
                                    minioService.rewriteTempMediaLinks(pair.getRightText(), mediaKey)
                            ))
                            .toList(),
                    minioService.rewriteTempMediaLinks(matching.getExplanation(), mediaKey)
            );
        } else {
            throw new GlobalException(ErrorCode.INVALID_QUESTION_TYPE);
        }
    }

    public List<QuestionCreationData> processForCreate(List<CreateQuestionRequest> requests, boolean useSubject, String mediaKey) {
        return requests.stream()
                .map(q -> processing(q, useSubject, mediaKey))
                .toList();
    }

    public List<QuestionCreationData> processForUpdate(List<UpdateQuestionRequest> requests, boolean useSubject, String mediaKey) {
        return requests.stream()
                .map(q -> processing(q, useSubject, mediaKey))
                .toList();
    }
}