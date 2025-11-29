package com.cmdlee.quizsushi.member.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class RefreshTokenData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String refreshUUID;
    private final String userId;
    private final String userAgent;
    private final Instant issuedAt;
}