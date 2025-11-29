package com.cmdlee.quizsushi.member.dto.response;


import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import com.cmdlee.quizsushi.member.domain.model.enums.PlanTier;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberMeResponse implements MeResponse {
    private Long id;
    private String email;
    private String nickname;
    private String avatar;
    private PlanTier planTier;

    public static MemberMeResponse from(QuizsushiMember member) {
        return MemberMeResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .avatar(member.getProfileImage())
                .planTier(member.getPlanTier())
                .build();
    }
}