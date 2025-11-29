package com.cmdlee.quizsushi.member.domain.model;

import com.cmdlee.quizsushi.member.domain.model.enums.OAuthProvider;
import com.cmdlee.quizsushi.member.dto.OAuthUserInfo;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "oauth_account")
@Getter
@NoArgsConstructor
public class OAuthAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oauth_accounts_seq")
    @SequenceGenerator(name = "oauth_accounts_seq", sequenceName = "oauth_accounts_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private QuizsushiMember member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OAuthProvider provider;

    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    @Builder
    public OAuthAccount(QuizsushiMember member, OAuthProvider provider, String providerId) {
        this.member = member;
        this.provider = provider;
        this.providerId = providerId;
    }

    public static OAuthAccount of(QuizsushiMember member, OAuthUserInfo userInfo) {
        return OAuthAccount.builder()
                .member(member)
                .provider(userInfo.getProvider())
                .providerId(userInfo.getProviderId())
                .build();
    }
}
