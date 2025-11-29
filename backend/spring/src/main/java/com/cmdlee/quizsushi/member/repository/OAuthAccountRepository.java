package com.cmdlee.quizsushi.member.repository;

import com.cmdlee.quizsushi.member.domain.model.OAuthAccount;
import com.cmdlee.quizsushi.member.domain.model.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
    Optional<OAuthAccount> findByProviderAndProviderId(OAuthProvider provider, String providerId);
}
