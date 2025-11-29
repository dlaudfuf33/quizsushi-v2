package com.cmdlee.quizsushi.quiz.service;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import com.cmdlee.quizsushi.member.repository.MemberRepository;
import com.cmdlee.quizsushi.minio.service.MinioService;
import com.cmdlee.quizsushi.quiz.domain.factory.QuizFactory;
import com.cmdlee.quizsushi.quiz.domain.model.*;
import com.cmdlee.quizsushi.quiz.domain.model.question.BaseQuestion;
import com.cmdlee.quizsushi.quiz.dto.QuizCreationData;
import com.cmdlee.quizsushi.quiz.dto.request.CreateQuizRequest;
import com.cmdlee.quizsushi.quiz.dto.request.QuizRatingRequest;
import com.cmdlee.quizsushi.quiz.dto.request.QuizResultRequest;
import com.cmdlee.quizsushi.quiz.dto.request.UpdateQuizRequest;
import com.cmdlee.quizsushi.quiz.dto.response.*;
import com.cmdlee.quizsushi.quiz.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final CategoryRepository categoryRepository;
    private final QuizRatingRepository quizRatingRepository;
    private final MemberRepository memberRepository;
    private final MemberQuizSolveLogRepository memberQuizSolveLogRepository;
    private final GuestQuizSolveLogRepository guestQuizSolveLogRepository;

    private final QuizPreprocessingService quizPreprocessingService;
    private final MinioService minioService;

    private final QuizFactory quizFactory;
    private final QuestionService questionService;


    public QuizPageResponse getQuizPage(int page, int size, String sortKey, String searchType, String query, Long categoryId) {
        Page<QuizSummaryResponse> pageResult;
        if (isSearchQuery(query)) {
            pageResult = searchQuizByQuery(page, size, sortKey, searchType, query, categoryId);
        } else {
            pageResult = getQuizWithoutQuery(page, size, sortKey, categoryId);
        }

        return new QuizPageResponse(pageResult.getContent(), pageResult.getNumber(),
                pageResult.getTotalPages(), pageResult.getTotalElements());
    }


    @Transactional
    public CreatedQuizResponse createQuiz(CreateQuizRequest request, Long memberId) {
        QuizsushiMember member = memberRepository.findById(memberId).orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));

        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(
                () -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));
        String mediaKey = NanoIdUtils.randomNanoId();

        QuizCreationData data = quizPreprocessingService.process(request, member, category, mediaKey);
        Quiz quiz = quizFactory.create(data);
        quizRepository.save(quiz);

        List<BaseQuestion> questions = questionService.createQuestions(request.getQuestions(), quiz);
        quiz.addQuestions(questions);

        return new CreatedQuizResponse(quiz.getId());
    }


    @Transactional
    public QuizDetailResponse getQuizById(Long id) {
        Quiz quiz = quizRepository.findQuizDetailById(id).orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));
        List<BaseQuestion> questions = questionService.findAllQuestionsByQuizId(id).stream()
                .sorted(Comparator.comparingInt(BaseQuestion::getNo))
                .toList();
        quiz.increaseViewCount();
        return QuizDetailResponse.of(quiz,questions);
    }

    @Transactional
    public UpdatedQuizResponse updateQuiz(UpdateQuizRequest request, long memberId) {
        Quiz quiz = quizRepository.findById(request.getId())
                .orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));

        if (!quiz.getAuthor().getId().equals(memberId)) {
            throw new GlobalException(ErrorCode.MEMBER_MISMATCH);
        }

        // 퀴즈 메타데이터 업데이트
        quiz.updateMetadata(request.getDescription(), request.isUseSubject(), request.getQuestions().size());

        List<BaseQuestion> updatedQuestions = questionService.updateQuestions(request.getQuestions(), quiz);
        quiz.clearQuestions();
        quiz.addQuestions(updatedQuestions);

        return new UpdatedQuizResponse(quiz.getId());
    }


    @Transactional
    public void deleteQuiz(Long quizId, long memberId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));
        if (quiz.getAuthor().getId() != memberId) {
            throw new GlobalException(ErrorCode.MEMBER_MISMATCH);
        }
        minioService.deleteAllWithPrefix(quiz.getMediaKey());
        quizRepository.delete(quiz);
    }

    @Transactional
    public void rateQuiz(Long quizId, Long memberId, QuizRatingRequest request) {
        Quiz quiz = quizRepository.findByIdWithRating(quizId).orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));
        QuizsushiMember member = memberRepository.findById(memberId).orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));

        Optional<QuizRating> existing = quizRatingRepository.findByQuizAndMember(quiz, member);

        if (existing.isPresent()) {
            QuizRating ratingEntity = existing.get();
            int old = ratingEntity.getRating();
            ratingEntity.updateRating(request.getRating());
            quiz.updateRate(old, request.getRating());
        } else {
            QuizRating newRatingEntity = QuizRating.builder().quiz(quiz).member(member).rating(request.getRating()).build();
            quizRatingRepository.save(newRatingEntity);
            quiz.addRate(newRatingEntity);
        }
    }

    @Transactional
    public void saveMemberQuizResult(Long quizId, Long memberId, QuizResultRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));

        QuizsushiMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));

        quiz.increaseSolveCount();

        MemberQuizSolveLog log = MemberQuizSolveLog.builder()
                .quiz(quiz)
                .member(member)
                .score(request.getScore())
                .build();

        memberQuizSolveLogRepository.save(log);
    }

    @Transactional
    public void saveGuestQuizResult(Long quizId, QuizResultRequest request, String guestIp, String guestUa) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));

        quiz.increaseSolveCount();

        GuestQuizSolveLog log = GuestQuizSolveLog.builder()
                .quiz(quiz)
                .score(request.getScore())
                .guestIp(guestIp)
                .guestUa(guestUa)
                .build();

        guestQuizSolveLogRepository.save(log);
    }

    private boolean isSearchQuery(String query) {
        return query != null && !query.trim().isEmpty();
    }

    private Page<QuizSummaryResponse> searchQuizByQuery(
            int page, int size, String sortKey, String searchType, String query, Long categoryId) {
        String likeQuery = "%" + query.trim() + "%";
        Pageable pageable = getPageable(page, size, sortKey);
        Page<Quiz> result;

        switch (searchType) {
            case "title" -> result = (categoryId != null)
                    ? quizRepository.searchQuizTitleWithCategory(likeQuery, categoryId, pageable)
                    : quizRepository.searchQuizTitleNoCategory(likeQuery, pageable);

            case "author" -> result = (categoryId != null)
                    ? quizRepository.searchQuizAuthorNameWithCategory(likeQuery, categoryId, pageable)
                    : quizRepository.searchQuizAuthorNameNoCategory(likeQuery, pageable);

            default -> throw new GlobalException(ErrorCode.INVALID_SEARCHTYPE);
        }

        return result.map(QuizSummaryResponse::from);
    }

    private Page<QuizSummaryResponse> getQuizWithoutQuery(int page, int size, String sortKey, Long categoryId) {
        Pageable pageable = getPageable(page, size, sortKey);
        Page<Quiz> result;

        if (categoryId != null) {
            result = quizRepository.findAllByCategoryId(categoryId, pageable);
        } else {
            result = quizRepository.findAll(pageable);
        }

        return result.map(QuizSummaryResponse::from);
    }

    private Pageable getPageable(int page, int size, String sortKey) {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, mapSortKey(sortKey)));
    }

    private String mapSortKey(String sortKey) {
        return switch (sortKey) {
            case "newest" -> "createdAt";
            case "rating" -> "rating";
            default -> "createdAt";
        };
    }
}
