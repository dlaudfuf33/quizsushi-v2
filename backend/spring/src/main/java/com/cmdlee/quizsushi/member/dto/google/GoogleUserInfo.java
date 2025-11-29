package com.cmdlee.quizsushi.member.dto.google;

import com.cmdlee.quizsushi.member.domain.model.enums.OAuthProvider;
import com.cmdlee.quizsushi.member.dto.OAuthUserInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleUserInfo implements OAuthUserInfo {
    private String id;
    private String email;
    private String nickname;

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.GOOGLE;
    }

    @Override
    public String getProviderId() {
        return id;
    }
}