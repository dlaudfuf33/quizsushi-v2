package com.cmdlee.quizsushi.member.dto;

import com.cmdlee.quizsushi.member.domain.model.enums.OAuthProvider;

public interface OAuthUserInfo {
    // "kakao", "google", ..
    OAuthProvider getProvider();

    // SNS 고유 ID
    String getProviderId();

    String getEmail();

    String getNickname();

}
