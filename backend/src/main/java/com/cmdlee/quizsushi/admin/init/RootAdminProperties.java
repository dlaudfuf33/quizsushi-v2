package com.cmdlee.quizsushi.admin.init;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "admin.root")
public class RootAdminProperties {
    @NotBlank(message = "ROOT 관리자 ID는 반드시 설정해야 합니다.")
    private String id;

    @NotBlank(message = "ROOT 관리자 비밀번호는 반드시 설정해야 합니다.")
    private String password;
}