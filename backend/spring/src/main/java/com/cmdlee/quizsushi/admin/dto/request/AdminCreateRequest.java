package com.cmdlee.quizsushi.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AdminCreateRequest {
    @NotBlank
    private String alias;
    @NotBlank
    private String username;
    @NotBlank
    private String rawPassword;
    @NotBlank
    private String role;
}