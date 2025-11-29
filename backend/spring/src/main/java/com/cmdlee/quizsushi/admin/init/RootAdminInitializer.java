package com.cmdlee.quizsushi.admin.init;

import com.cmdlee.quizsushi.admin.domain.model.AdminMember;
import com.cmdlee.quizsushi.admin.domain.model.enums.AdminRole;
import com.cmdlee.quizsushi.admin.repository.AdminMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RootAdminInitializer implements CommandLineRunner {

    private final AdminMemberRepository adminMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RootAdminProperties properties;

    @Override
    public void run(String... args) {
        String rootId = properties.getId();
        String rawPassword = properties.getPassword();

        if (!adminMemberRepository.existsByRole(AdminRole.ROOT)) {
            AdminMember root = AdminMember.builder()
                    .alias("ROOT 관리자 계정")
                    .username(rootId)
                    .password(passwordEncoder.encode(rawPassword))
                    .role(AdminRole.ROOT)
                    .build();
            adminMemberRepository.save(root);
            log.info("First \"ROOT_ADMIN\" ACCOUNT CREATED.");
        }
    }
}