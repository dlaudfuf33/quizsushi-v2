package com.cmdlee.quizsushi.member.service;

import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.member.domain.model.OAuthAccount;
import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import com.cmdlee.quizsushi.member.dto.OAuthUserInfo;
import com.cmdlee.quizsushi.member.dto.request.UpdateProfileRequest;
import com.cmdlee.quizsushi.member.dto.response.*;
import com.cmdlee.quizsushi.member.repository.MemberRepository;
import com.cmdlee.quizsushi.member.repository.OAuthAccountRepository;
import com.cmdlee.quizsushi.quiz.domain.model.MemberQuizSolveLog;
import com.cmdlee.quizsushi.quiz.domain.model.Quiz;
import com.cmdlee.quizsushi.quiz.repository.MemberQuizSolveLogRepository;
import com.cmdlee.quizsushi.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final OAuthAccountRepository oauthAccountRepository;
    private static final List<String> DEFAULT_PROFILE_IMAGES = List.of(
            "https://minio.cmdlee.com/quizsushi/public/default/profiles/rice.webp",
            "https://minio.cmdlee.com/quizsushi/public/default/profiles/egg.webp",
            "https://minio.cmdlee.com/quizsushi/public/default/profiles/tuna.webp"
    );
    private final MemberQuizSolveLogRepository quizSolveLogRepository;
    private final QuizRepository quizRepository;

    public QuizsushiMember findMemberById(long memberId) {
        return memberRepository.findById(memberId).orElseThrow(
                () -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));
    }

    @Transactional
    public QuizsushiMember findOrCreateByOAuth(OAuthUserInfo userInfo) {
        int index = ThreadLocalRandom.current().nextInt(DEFAULT_PROFILE_IMAGES.size());
        String randomProfileImage = DEFAULT_PROFILE_IMAGES.get(index);

        return oauthAccountRepository.findByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId())
                .map(account -> {
                    QuizsushiMember member = account.getMember();
                    if (member.isBan()) {
                        throw new GlobalException(ErrorCode.BANNED_MEMBER);
                    }
                    return member;
                })
                .orElseGet(() -> {
                    QuizsushiMember newMember = QuizsushiMember.of(userInfo, randomProfileImage);
                    memberRepository.save(newMember);
                    OAuthAccount newAccount = OAuthAccount.of(newMember, userInfo);
                    oauthAccountRepository.save(newAccount);
                    return newMember;
                });
    }

    public MeResponse getMemberMe(long memberId) {
        QuizsushiMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));
        return MemberMeResponse.from(member);
    }


    public MemberProfileResponse getProfile(long memberId) {
        QuizsushiMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));

        List<MemberQuizSolveLog> quizSolveLog = quizSolveLogRepository.findByMember(member);
        int solved = quizSolveLog.size();
        double avgScore = Math.round(
                quizSolveLog.stream()
                        .mapToInt(MemberQuizSolveLog::getScore)
                        .average()
                        .orElse(0.0) * 100
        ) / 100.0;
        int created = quizRepository.countByAuthorId(member.getId());

        return MemberProfileResponse.of(member, created, solved, avgScore);
    }

    public SolvedQuizPageResponse getSolvedQuizzes(Long memberId, int page, int size) {
        QuizsushiMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<MemberQuizSolveLog> logPage = quizSolveLogRepository.findByMember(member, pageable);

        return new SolvedQuizPageResponse(
                logPage.map(SolvedQuizResponse::from).toList(),
                logPage.getNumber(),
                logPage.getTotalPages(),
                logPage.getTotalElements()
        );
    }

    public CreatedQuizPageResponse getCreatedQuizzes(Long memberId, int page, int size) {
        QuizsushiMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Quiz> quizPage = quizRepository.findCreatedByAuthor(member.getId(), pageable);

        return new CreatedQuizPageResponse(
                quizPage.map(CreatedQuizResponse::from).toList(),
                quizPage.getNumber(),
                quizPage.getTotalPages(),
                quizPage.getTotalElements()
        );
    }

    @Transactional
    public void updateProfile(Long memberId, UpdateProfileRequest request) {
        QuizsushiMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));
        member.updateProfile(request);
    }

    @Transactional
    public void deleteMe(Long memberId) {
        QuizsushiMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));

        if (member.isDeleted()) {
            throw new GlobalException(ErrorCode.MEMBER_ALREADY_DELETED);
        }

        List<Quiz> quizzes = quizRepository.findByAuthor(member);
        quizRepository.deleteAll(quizzes);
        member.deactivate();
    }
}
