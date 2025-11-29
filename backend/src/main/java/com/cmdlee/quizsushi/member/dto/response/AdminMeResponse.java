package com.cmdlee.quizsushi.member.dto.response;

import com.cmdlee.quizsushi.admin.domain.model.AdminMember;
import com.cmdlee.quizsushi.admin.domain.model.enums.AdminRole;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminMeResponse implements MeResponse {
    private Long id;
    private String username;
    private String alias;
    private AdminRole role;

    public static AdminMeResponse from(AdminMember admin) {
        return AdminMeResponse.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .alias(admin.getAlias())
                .role(admin.getRole())
                .build();
    }
}
