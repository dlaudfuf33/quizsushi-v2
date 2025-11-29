package com.cmdlee.quizsushi.admin.domain.model;

import com.cmdlee.quizsushi.admin.domain.model.enums.AdminRole;
import com.cmdlee.quizsushi.quiz.domain.model.TimeBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "admin_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminMember extends TimeBaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "admin_member_seq")
    @SequenceGenerator(name = "admin_member_seq", sequenceName = "admin_member_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 50)
    private String alias;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    @ToString.Exclude
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminRole role;

    public void updateRole(AdminRole role) {
        this.role = role;
    }

    @Builder
    public AdminMember(Long id, String alias, String username, String password, AdminRole role) {
        this.alias = alias;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public void updateInfo(String alias, String password) {
        if (alias != null && !alias.isBlank()) {
            this.alias = alias;
        }
        if (password != null && !password.isBlank()) {
            this.password = password;
        }
    }

}
