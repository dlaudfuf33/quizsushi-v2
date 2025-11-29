package com.cmdlee.quizsushi.member.dto.response;

import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import com.cmdlee.quizsushi.member.domain.model.enums.PlanTier;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberProfileResponse {
    private Long id;
    private String email;
    private String nickname;
    private PlanTier planTier;
    private String gender;
    private String avatar;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime createAt;
    private Stats stats;


    public static MemberProfileResponse of(QuizsushiMember member,
                                           int totalCreated,
                                           int totalSolved,
                                           double avgScore
    ) {
        return MemberProfileResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .planTier(member.getPlanTier())
                .gender(member.getGender())
                .avatar(member.getProfileImage())
                .birthDate(member.getBirthDate())
                .createAt(member.getCreatedAt())
                .stats(Stats.builder()
                        .totalQuizzesSolved(totalSolved)
                        .totalQuizzesCreated(totalCreated)
                        .averageScore(avgScore)
                        .build())
                .build();
    }
}
