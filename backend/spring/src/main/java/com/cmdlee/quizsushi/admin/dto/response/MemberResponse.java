package com.cmdlee.quizsushi.admin.dto.response;

import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class MemberResponse {
    private long id;
    private String email;
    private String nickname;
    private String planTier;
    private String createAt;
    private String updateAt;
    private String status;

    public static MemberResponse from(QuizsushiMember member) {
        String status = member.isDeleted() ? "탈퇴" : member.isBan() ? "정지" : "활성";
        return MemberResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .planTier(member.getPlanTier().name())
                .createAt(member.getCreatedAt().format(DateTimeFormatter.ofPattern("YYYY-MM-DD")))
                .updateAt(member.getUpdatedAt().format(DateTimeFormatter.ofPattern("YYYY-MM-DD")))
                .status(status)
                .build();
    }
}
