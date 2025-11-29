package com.cmdlee.quizsushi.admin.domain.model.enums;

import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import lombok.Getter;

@Getter
public enum AdminRole {
    ROOT,
    ADMIN,
    MANAGER,
    VIEWER;

    public static AdminRole from(String name) {
        try {
            return AdminRole.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
