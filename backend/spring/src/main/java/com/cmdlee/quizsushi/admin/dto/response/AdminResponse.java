package com.cmdlee.quizsushi.admin.dto.response;

import com.cmdlee.quizsushi.admin.domain.model.AdminMember;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminResponse {
    private Long id;
    private String alias;
    private String username;
    private String role;

    public static AdminResponse form(AdminMember admin) {
        return AdminResponse.builder()
                .id(admin.getId())
                .alias(admin.getAlias())
                .username(admin.getUsername())
                .role(admin.getRole().name())
                .build();
    }
}
