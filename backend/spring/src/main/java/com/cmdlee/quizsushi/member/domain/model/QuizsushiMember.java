package com.cmdlee.quizsushi.member.domain.model;

import com.cmdlee.quizsushi.member.domain.model.enums.PlanTier;
import com.cmdlee.quizsushi.member.dto.OAuthUserInfo;
import com.cmdlee.quizsushi.member.dto.request.UpdateProfileRequest;
import com.cmdlee.quizsushi.quiz.domain.model.TimeBaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizsushi_member")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizsushiMember extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "quizsushi_member_seq")
    @SequenceGenerator(name = "quizsushi_member_seq", sequenceName = "quizsushi_member_seq", allocationSize = 1)
    private Long id;

    @Column(length = 100)
    private String email;

    @Column(nullable = false)
    private String profileImage;

    @Column(length = 50, nullable = false)
    private String nickname;

    @Column(nullable = true)
    private LocalDate birthDate;

    @Column(nullable = false)
    private String gender;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanTier planTier = PlanTier.FREE;

    @Builder.Default
    @Column(nullable = false)
    private boolean isDeleted = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean isBan = false;

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OAuthAccount> oauthAccounts = new ArrayList<>();

    public static QuizsushiMember of(OAuthUserInfo userInfo, String profileUrl) {
        return QuizsushiMember.builder()
                .email(userInfo.getEmail())
                .nickname(userInfo.getNickname())
                .profileImage(profileUrl)
                .gender("unknown")
                .planTier(PlanTier.FREE)
                .build();
    }

    public void updateProfile(UpdateProfileRequest request) {
        this.nickname = request.getNickname();
        this.birthDate = request.getLocalDateBirth();
        this.gender = request.getGender();
    }

    public void deactivate() {
        this.email = this.nickname = this.profileImage = "==DELETED==";
        this.gender = "unknown";
        this.birthDate = null;
        this.isDeleted = true;
        this.oauthAccounts.clear();
    }

    public void ban() {
        this.isBan = true;
    }

    public void unBan() {
        this.isBan = false;
    }

    public void banToggle() {
        if (this.isBan) {
            this.unBan();
        } else {
            this.ban();
        }
    }
}