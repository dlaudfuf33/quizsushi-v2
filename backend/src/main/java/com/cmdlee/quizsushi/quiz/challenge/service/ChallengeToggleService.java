package com.cmdlee.quizsushi.quiz.challenge.service;

import com.cmdlee.quizsushi.admin.domain.model.AdminMember;
import com.cmdlee.quizsushi.admin.repository.AdminMemberRepository;
import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.quiz.challenge.model.ChallengeToggle;
import com.cmdlee.quizsushi.quiz.challenge.repository.ChallengeToggleRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChallengeToggleService {

    private final ChallengeToggleRepository challengeToggleRepository;
    private final AdminMemberRepository adminMemberRepository;
    private volatile boolean challengeEnabled = false;

    @PostConstruct
    public void initToggleStatus() {
        this.challengeEnabled = challengeToggleRepository.findTopByOrderByUpdatedAtDesc()
                .map(ChallengeToggle::getIsEnabled)
                .orElse(false);
    }

    @Transactional
    public void toggleChallenge(Long adminId) {
        boolean newStatus = !challengeEnabled;
        AdminMember admin = adminMemberRepository.findById(adminId)
                .orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));

        ChallengeToggle toggle = ChallengeToggle.of(newStatus, admin);
        challengeToggleRepository.save(toggle);
        this.challengeEnabled = newStatus;
    }

    public boolean getCurrentStatus() {
        return challengeEnabled;
    }
}
