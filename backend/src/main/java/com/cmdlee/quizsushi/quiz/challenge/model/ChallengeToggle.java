package com.cmdlee.quizsushi.quiz.challenge.model;

import com.cmdlee.quizsushi.admin.domain.model.AdminMember;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeToggle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean isEnabled;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private AdminMember updatedBy;

    public static ChallengeToggle of(Boolean enabled, AdminMember admin) {
        ChallengeToggle toggle = new ChallengeToggle();
        toggle.isEnabled = enabled;
        toggle.updatedBy = admin;
        toggle.updatedAt = LocalDateTime.now();
        return toggle;
    }
}
